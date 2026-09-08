# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

A multi-module Maven project simulating a distributed order/inventory system, built to practice senior-level Java/Spring backend patterns: saga-based distributed transactions, event-driven architecture, resilience patterns, and safe concurrent resource handling.

**Domain**: a customer places an order → stock is reserved → payment is processed (future module) → order is confirmed or compensated (cancelled + stock released) if any step fails.

## Module Structure

```
orders-system/                    (parent pom, packaging=pom)
├── orders-system-core/           (library — shared, no Spring Boot app context)
├── orders-service/                (Spring Boot app)
├── stocking-service/               (Spring Boot app)
└── docker-compose.yml             (Postgres x2, Kafka, Redis/Memcached — local infra)
```

### `orders-system-core`
Shared library with **no** framework runtime dependencies beyond what's needed to define contracts. Contains:
- **Event contracts**: `OrderCreatedEvent`, `StockReservedEvent`, `StockReservationFailedEvent`, etc. (as Java records) — the shared "wire format" both services (de)serialize.
- **Common value objects**: `Money`, `Sku`, `OrderId` (typed IDs over raw `String` to avoid primitive obsession).
- **Shared exceptions**: base types like `DomainException` that both services can extend.
- **Kafka topic name constants**: single source of truth so services don't hardcode topic strings independently.
  Rule: this module must **not** depend on `orders-service` or `stocking-service` — dependency direction is always inward, from the apps to the core, never the reverse. It should also stay framework-light (avoid pulling in `spring-boot-starter-web` here); if serialization annotations are needed, prefer plain Jackson annotations over anything Spring-specific.

### `orders-service`
Owns the `Order` aggregate and the **saga orchestrator**. Persists orders and saga state (`saga_instance` table: `CREATED → STOCK_RESERVED → PAID → CONFIRMED`, or a `CANCELLING/CANCELLED` branch). Exposes the customer-facing REST API.

### `stocking-service`
Owns `Stock` per SKU. Exposes reserve/release endpoints (or Kafka command listeners, depending on which saga style is active — see Architecture Notes). Enforces available-quantity invariants at the DB level.

## Tech Stack

- **Java 21**, Maven (multi-module, single parent `pom.xml` managing dependency versions via `<dependencyManagement>`)
- **Spring Boot 3.x**
- **Spring Data JPA** — used in `orders-service` for the `Order`/`OrderLine`/`saga_instance` aggregate (relational, needs joins and transactional consistency across an aggregate)
- **Spring Data JDBC** — used in `stocking-service` for `Stock` (simple, single-aggregate-root entity with no complex object graph — JDBC's lighter model fits; also avoids Hibernate's dirty-checking/lazy-loading surprises for a table that's mutated via atomic conditional updates rather than entity mutation, see Architecture Notes)
- **Spring Web** (`spring-boot-starter-web`, blocking MVC — not WebFlux, unless a specific module later needs reactive)
- **Spring Kafka** for event publishing/consuming
- **Spring Cache** abstraction (`@Cacheable`/`@CacheEvict`) backed by Redis (preferred) or Memcached — used for read-heavy, rarely-changing lookups (e.g., product/SKU metadata), **never** for the live `available` stock count, which must always be read from the source of truth to avoid overselling
- **Flyway** for schema migrations, one migration set per service (each owns its own schema/database — no shared DB across services)
- **Testcontainers** (Postgres, Kafka, Redis) for integration tests
- **JUnit 5 + Mockito** for unit tests
- **Resilience4j** for circuit breaker/retry on any synchronous inter-service calls
- **springdoc-openapi** for API docs on both services
## Architecture Notes (for consistency across the codebase)

### Saga pattern
The system is being built incrementally: choreography first (each service reacts to events independently), then refactored to orchestration (an explicit `OrderSagaOrchestrator` in `orders-service` driving each step and calling compensations). When working on saga logic:
- Prefer commands/events over synchronous calls between `orders-service` and `stocking-service` wherever the step doesn't need an immediate response.
- Every saga step must be **idempotent** — consumers may see the same event more than once (at-least-once delivery), so handlers should be safe to re-run (e.g., check current state before applying a transition, or rely on a unique constraint).
- Every compensation must be equally reliable — don't assume the "undo" path is less important to test than the happy path.
### Locking / concurrency on `Stock`
Stock reservation and release use **atomic conditional updates** (`UPDATE stock SET available = available - :qty WHERE sku = :sku AND available >= :qty`, checking rows-affected), not application-level pessimistic or optimistic locking. This is a deliberate choice for the single-field decrement/increment case — see the study guide §6.4 for the full rationale and when pessimistic/optimistic locking would be the right call instead (multi-field read-modify-write with business logic in between). Don't introduce a distributed lock (Redis Redlock, etc.) for stock — the database is the single source of truth and is sufficient.

### Idempotency at the API boundary
`POST /api/orders` requires an `Idempotency-Key` header; duplicate keys return the original response rather than creating a second order.

### Correlation IDs
A `traceId` is generated at the API boundary in `orders-service`, propagated through Kafka event headers/payloads, and logged by every service that touches a given saga instance — this is how a single order's flow is traced across services.

## Build & Common Commands

```bash
# Build everything (from repo root)
mvn clean install
 
# Build a single module (core must be installed first if changed)
mvn -pl orders-system-core install
mvn -pl orders-service -am install
 
# Run tests only
mvn test
 
# Run a specific service locally
cd orders-service && mvn spring-boot:run
 
# Bring up local infra (Postgres, Kafka, Redis) without the services
docker-compose up postgres-orders postgres-stock kafka redis
 
# Bring up the full system
docker-compose up
```

## Testing Conventions

- Unit tests: standard `*Test.java`, mock collaborators with Mockito, no Spring context.
- Integration tests: `*IT.java`, use `@SpringBootTest` + Testcontainers, one test class per service verifying it against real Postgres/Kafka rather than mocks.
- Saga tests specifically must cover **both** the happy path and at least one compensation path per saga — a saga test suite that only exercises the happy path is considered incomplete.
- Don't write tests against `orders-system-core` directly unless it contains actual logic (e.g., a value object's validation) — pure data-carrier records don't need dedicated tests.
## Code Conventions

- Constructor injection only — no field `@Autowired`.
- Package-by-feature within each service (e.g., `order/`, `saga/`, `inventory-client/`) rather than package-by-layer (`controller/`, `service/`, `repository/` at the top level).
- Domain/aggregate logic lives on the entity/aggregate itself where reasonable (e.g., `Order.addLine()` enforces its own invariants) rather than leaking validation into service classes.
- Records for all event/DTO types; classes reserved for entities and services with actual behavior.
- No `Optional` as a field type or method parameter — only as a return type.
## What NOT to do

- Don't add a shared database between `orders-service` and `stocking-service` — each service owns its data, communication is via events/API only.
- Don't put Spring Boot annotations or REST controllers in `orders-system-core` — it's a plain library.
- Don't reach for optimistic/pessimistic locking on `Stock` reservation — use the atomic conditional update pattern described above unless a genuinely multi-field transaction requires otherwise.
- Don't skip compensation-path tests when adding new saga steps.
 