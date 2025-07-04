package com.winnguyen1905.product.core.model.response;

import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record BrandResponse(
    UUID id,
    String name,
    String description,
    Boolean isVerified,
    String createdDate,
    String updatedDate) implements AbstractModel {
  @Builder
  public BrandResponse(
      UUID id,
      String name,
      String description,
      Boolean isVerified,
      String createdDate,
      String updatedDate) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.isVerified = isVerified;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
  }
} 
