package com.winnguyen1905.product.core.model;

import java.util.List;

import lombok.*;

@Getter
@Setter
public class Product extends BaseObject<Product> {
  private String name;

  private String thumb;

  private String productType;

  private String description;

  private Double price;

  private String slug;

  private List<Variation> variations;
}
