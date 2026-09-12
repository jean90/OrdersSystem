package com.amazingco.orders;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for {@code *IT} tests that need a real Postgres. Uses the Testcontainers
 * "singleton container" pattern deliberately, not {@code @Testcontainers}/{@code @Container}:
 * those annotations tie a container's start/stop to a single test class's lifecycle, which
 * would stop it (breaking every other IT subclass sharing this base) as soon as the first
 * subclass finishes. Starting it once in a static initializer and never stopping it lets it
 * outlive every subclass for the duration of the JVM; Testcontainers' Ryuk reaper cleans it
 * up when the build's JVM exits.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractPostgresIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("orders")
                .withUsername("orders")
                .withPassword("orders");
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
