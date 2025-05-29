package com.winnguyen1905.product.common.constant;

public enum ProductType {
  ELECTRONIC("electronic"),
  FURNITURE("furniture"),
  FASHION("fashion"),
  ACCESSORY("accessory");

  private final String type;

  ProductType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return type;
  }

  public static ProductType fromString(String type) {
    for (ProductType productType : ProductType.values()) {
      if (productType.type.equalsIgnoreCase(type)) {
        return productType;
      }
    }
    throw new IllegalArgumentException("Unknown product type: " + type);
  }
}
