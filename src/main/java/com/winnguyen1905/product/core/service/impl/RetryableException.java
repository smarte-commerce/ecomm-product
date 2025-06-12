package com.winnguyen1905.product.core.service.impl;

public class RetryableException extends RuntimeException {
  public RetryableException(String message, Throwable cause) {
      super(message, cause);
  }
}
