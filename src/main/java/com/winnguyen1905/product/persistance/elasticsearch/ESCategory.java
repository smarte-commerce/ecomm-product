package com.winnguyen1905.product.persistance.elasticsearch;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.winnguyen1905.product.persistance.entity.EBaseAudit;

import lombok.*;

@Getter
@Setter
@Builder
public class ESCategory {
  @Id
  private UUID id;

  private String name;

  private Long left;

  private Long right;

  private String description;

  private UUID parentId;

  private UUID shopId;
}
