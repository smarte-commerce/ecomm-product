package com.winnguyen1905.product.common;

public enum ApplyDiscountType {
    ALL("all"),
    SPECIFIC("specific");

    private final String applyDiscountType;

    ApplyDiscountType(String applyDiscountType) {
        this.applyDiscountType = applyDiscountType;
    }

    public String getApplyDiscountType() {
        return applyDiscountType;
    }
}
