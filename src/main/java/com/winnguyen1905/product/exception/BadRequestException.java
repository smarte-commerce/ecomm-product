package com.winnguyen1905.product.exception;

import com.winnguyen1905.product.secure.BaseException;

public class BadRequestException extends BaseException {
  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, int code) {
    super(message, code);
  }
}
