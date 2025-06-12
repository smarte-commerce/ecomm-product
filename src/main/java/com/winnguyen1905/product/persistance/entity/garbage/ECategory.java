package com.winnguyen1905.product.persistance.entity.garbage;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "category")
public class ECategory implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "code")
  private String code;

  @Column(name = "is_published")
  private Boolean isPublished;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", name = "product_features")
  private Object features;


  // @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  // private List<EProduct> products;

  // @PrePersist
  // private void prePersist() {
  //   if (this.isPublished == null) {
  //     this.isPublished = true;
  //   }
  // }
}
