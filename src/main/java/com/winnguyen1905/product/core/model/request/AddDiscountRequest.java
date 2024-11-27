package com.winnguyen1905.product.core.model.request;

import java.time.Instant;
import java.util.Set;

import com.winnguyen1905.product.common.ApplyDiscountType;
import com.winnguyen1905.product.common.DiscountType;
import com.winnguyen1905.product.core.model.Product;

import jakarta.validation.constraints.NotBlank;

public class AddDiscountRequest {
  private String name;

  private String description;

  private DiscountType discountType;

  private Number value;

  @NotBlank
  private String code;

  private Instant startDate;

  private Instant endDate;

  private Integer maxUses;

  private Integer usesCount;

  private Integer maxUsesPerUser;

  private Double minOrderValue;

  private Boolean isActive;

  private ApplyDiscountType appliesTo;

  private Set<Product> products;

  private Product productList;
}
