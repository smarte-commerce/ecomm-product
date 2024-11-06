package com.winnguyen1905.technologystore.entity;

import com.winnguyen1905.technologystore.common.ProductTypeConstant;
import com.winnguyen1905.technologystore.entity.base.BaseEntityAudit;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "footwears")
@PrimaryKeyJoinColumn(name = "footwear_id")
@DiscriminatorValue(ProductTypeConstant.FOOTWEAR)
public class FootwearEntity extends BaseEntityAudit {

}