package com.winnguyen1905.product.persistance.entity;

 
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class ECategory extends EBaseAudit {
    
}
