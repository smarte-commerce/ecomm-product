package com.winnguyen1905.product.exception;

import com.winnguyen1905.product.secure.BaseException;

public class ResourceNotFoundException extends BaseException {
  public ResourceNotFoundException(String message, Integer code, Object error) {
    super(message, code, error);
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
