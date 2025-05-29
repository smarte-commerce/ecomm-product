package com.winnguyen1905.product.persistance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "inventory", schema = "ecommerce")
public class EInventory implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "is_deleted", columnDefinition = "BIT(1)")
  private Boolean isDeleted;

  // @Column(name = "created_by")
  // private String createdBy;

  // @Column(name = "updated_by")
  // private String updatedBy;

  // @CreationTimestamp
  // @Column(name = "created_date")
  // private Instant createdDate;

  // @UpdateTimestamp
  // @Column(name = "updated_date")
  // private Instant updatedDate;

  @Column(name = "sku")
  private String sku;

  @ManyToOne
  @JoinColumn(name = "product_id", columnDefinition = "BINARY(16)")
  private EProduct product;

  @Column(name = "quantity_available")
  private Integer quantityAvailable;

  @Column(name = "quantity_reserved")
  private Integer quantityReserved;

  @Column(name = "quantity_sold")
  private Integer quantitySold;
  
  @Column(name = "address")
  private String address;
}
