package com.amazingco.core.stock;

import com.amazingco.core.exception.DomainException;

public class InvalidReservationStateException extends DomainException {

    private final ReservationId reservationId;
    private final ReservationStatus currentStatus;
    private final ReservationStatus attemptedStatus;

    public InvalidReservationStateException(ReservationId reservationId, ReservationStatus currentStatus,
                                              ReservationStatus attemptedStatus) {
        super("Cannot transition reservation " + reservationId.value() + " from " + currentStatus
                + " to " + attemptedStatus);
        this.reservationId = reservationId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    public ReservationId reservationId() {
        return reservationId;
    }

    public ReservationStatus currentStatus() {
        return currentStatus;
    }

    public ReservationStatus attemptedStatus() {
        return attemptedStatus;
    }
}
