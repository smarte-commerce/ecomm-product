package com.winnguyen1905.product.persistance.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_discounts")
public class EUserDiscount extends EBaseAudit {

  public static enum Role {
    USER, CREATOR
  }

  @ManyToOne
  @JoinColumn(name = "discount_id")
  private EDiscount discount;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "remaining_usage")
  private Long remainingUsage;

  @Column(name = "user_role")
  @Enumerated(EnumType.STRING)
  private Role role;

}
