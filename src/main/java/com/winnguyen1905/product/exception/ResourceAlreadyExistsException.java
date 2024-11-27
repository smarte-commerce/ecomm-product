package com.winnguyen1905.product.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
  public ResourceAlreadyExistsException(String message) {
      super(message);
  }
}
