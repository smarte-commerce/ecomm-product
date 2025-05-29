package com.winnguyen1905.product.exception;

import com.winnguyen1905.product.secure.BaseException;

public class S3FileException extends BaseException {
  public S3FileException(String message, int code, Object error) {
    super(message, code, error);
  }

  public S3FileException(String message) {
    super(message, 500);
  }
}
