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
@Table(name = "inventory", schema = "public")
public class EInventory {
  @Id
  // @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @UpdateTimestamp
  @Column(name = "updated_date")
  private Instant updatedDate;

  @Column(name = "sku")
  private String sku;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private EProduct product;

  @Column(name = "quantity_available")
  private Integer quantityAvailable;

  @Column(name = "quantity_reserved")
  private Integer quantityReserved;

  @Column(name = "quantity_sold")
  private Integer quantitySold;

  @Column(name = "address")
  private String address;

  @PrePersist
  protected void prePersist() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }
}
