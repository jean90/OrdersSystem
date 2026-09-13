package com.amazingco.core.ordertransaction;

/**
 * The full orchestration state machine per the architecture notes. This slice only ever drives
 * {@code CREATED -> STOCK_RESERVED} or {@code -> CANCELLED} — {@code PAID}/{@code CONFIRMED}
 * are modeled here for schema completeness but nothing transitions to them yet (no payment
 * module exists).
 */
public enum OrderTransactionStatus {
    CREATED,
    STOCK_RESERVED,
    PAID,
    CONFIRMED,
    CANCELLING,
    CANCELLED
}
