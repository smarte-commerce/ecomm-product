package com.winnguyen1905.product.core.model.request.service;

import java.util.List;

import com.winnguyen1905.product.core.model.BaseObject;

import lombok.*;

@Getter
@Setter
public class SearchProductRequest extends BaseObject<SearchProductRequest> {
  private List<String> typeCode;

  private String name;

  private Double priceFrom;

  private Double priceTo;

  private String brand;

  private String os;

  private Integer ram;

  private Integer rom;

  private Integer pin;

  private String cpu;

  private String gpu;

  private String chipset;

  private Double xSize;

  private Double ySize;

  private String panelType;

  private String resolution;

  private Boolean isDraft;

  private Boolean isPublished;
}
