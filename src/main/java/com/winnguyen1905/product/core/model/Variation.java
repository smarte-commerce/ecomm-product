package com.winnguyen1905.product.core.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Variation extends AbstractModel {
  private String detail;
  private List<Inventory> inventories;
}
