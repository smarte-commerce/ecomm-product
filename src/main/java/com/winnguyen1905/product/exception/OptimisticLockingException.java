package com.winnguyen1905.product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Concurrent modification detected")
public class OptimisticLockingException extends RuntimeException {
    public OptimisticLockingException(String message) {
        super(message);
    }

    public OptimisticLockingException(String message, Throwable cause) {
        super(message, cause);
    }
}
