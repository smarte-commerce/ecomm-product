package com.winnguyen1905.product.exception;

public class ReservationException extends RuntimeException {
    
    public ReservationException(String message) {
        super(message);
    }
    
    public ReservationException(String message, Throwable cause) {
        super(message, cause);
    }
} 
