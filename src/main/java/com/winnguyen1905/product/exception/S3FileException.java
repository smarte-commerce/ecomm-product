package com.winnguyen1905.product.exception;

public class S3FileException extends BaseException {
  public S3FileException(String message, int code, Object error) {
    super(message, code, error);
  }

  public S3FileException(String message) {
    super(message, 500);
  }
}
