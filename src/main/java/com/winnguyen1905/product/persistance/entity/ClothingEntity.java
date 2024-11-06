package com.winnguyen1905.technologystore.entity;

 
import com.winnguyen1905.product.core.common.ProductTypeConstant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "clothings")
@DiscriminatorValue(ProductTypeConstant.CLOTHING)
@PrimaryKeyJoinColumn(name = "clothing_id")
public class ClothingEntity extends ProductEntity {
    
}