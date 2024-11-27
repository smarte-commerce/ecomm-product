package com.winnguyen1905.product.util;

import java.time.Instant;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FutureInstantValidator implements ConstraintValidator<FutureInstant, Instant> {
    @Override
    public void initialize(FutureInstant constraintAnnotation) {
    }

    @Override
    public boolean isValid(Instant time, ConstraintValidatorContext context) {
        if (time == null) {
            return true;
        }
        return time.isAfter(Instant.now());
    }
}
