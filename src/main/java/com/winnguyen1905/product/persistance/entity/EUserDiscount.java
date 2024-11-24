package com.winnguyen1905.product.persistance.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_discounts")
public class EUserDiscount extends EBaseAudit {
  @Column(name = "discount_id")
  private UUID discountId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "remaining_usage")
  private Long remainingUsage;

  private String role;
}
