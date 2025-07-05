package com.winnguyen1905.product.config;

import java.util.UUID;

public class ReservationExpiredEvent {
  private final UUID reservationId;

  public ReservationExpiredEvent(UUID reservationId) {
    this.reservationId = reservationId;
  }

  public UUID getReservationId() {
    return reservationId;
  }
}
