package com.winnguyen1905.product.persistance.elasticsearch;

import java.util.UUID;

import com.winnguyen1905.product.persistance.entity.EBaseAudit;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder 
public class ESCategory extends EBaseAudit {
  private String name;

  private Long left;

  private Long right;

  private String description;

  private UUID parentId;
  
  private UUID shopId;
}
