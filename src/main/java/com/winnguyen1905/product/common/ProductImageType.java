package com.winnguyen1905.product.common;

public enum ProductImageType {
  PRODUCT_VARIANT_IMAGE("product_variant_image"),
  PRODUCT_PREVIEW_IMAGE("product_preview_image"),
  PRODUCT_MANUFACTURER_IMAGE("manufacturer_image");

  private final String type;

  ProductImageType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return type;
  }

  public static ProductImageType fromString(String type) {
    for (ProductImageType imageType : ProductImageType.values()) {
      if (imageType.type.equalsIgnoreCase(type)) {
        return imageType;
      }
    }
    throw new IllegalArgumentException("Unknown image type: " + type);
  }
}
