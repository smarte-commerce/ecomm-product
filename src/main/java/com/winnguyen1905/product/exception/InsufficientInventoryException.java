package com.winnguyen1905.product.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Insufficient inventory")
public class InsufficientInventoryException extends RuntimeException {
  private final String productId;
  private final String sku;
  private final int quantityAvailable;
  private final int quantityReserved;
  private final int quantitySold;
  private final int quantityRequested;

  public InsufficientInventoryException(String message, String productId, String sku,
      int quantityAvailable, int quantityReserved, int quantitySold,
      int quantityRequested) {
    super(message);
    this.productId = productId;
    this.sku = sku;
    this.quantityAvailable = quantityAvailable;
    this.quantityReserved = quantityReserved;
    this.quantitySold = quantitySold;
    this.quantityRequested = quantityRequested;
  }

  public InsufficientInventoryException(String message, String sku, int quantityAvailable, int quantityRequested) {
    this(message, null, sku, quantityAvailable, 0, 0, quantityRequested);
  }

  public InsufficientInventoryException(String message, String sku, int quantityAvailable, int quantityRequested,
      Throwable cause) {
    super(message, cause);
    this.productId = null;
    this.sku = sku;
    this.quantityAvailable = quantityAvailable;
    this.quantityReserved = 0;
    this.quantitySold = 0;
    this.quantityRequested = quantityRequested;
  }

  @Override
  public String getMessage() {
    return String.format("%s - SKU: %s, Available: %d, Requested: %d, Reserved: %d, Sold: %d",
        super.getMessage(), sku, quantityAvailable, quantityRequested, quantityReserved, quantitySold);
  }
}
