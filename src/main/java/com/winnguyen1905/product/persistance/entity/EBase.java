package com.winnguyen1905.product.persistance.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public abstract class EBase implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UUID id;

  @Column(name = "is_deleted", updatable = true)
  private Boolean isDeleted;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof EBase that))
      return false;
    return id.equals(that.id);
  }

  @Serial private static final long serialVersionUID = -863164858986274318L;
}
