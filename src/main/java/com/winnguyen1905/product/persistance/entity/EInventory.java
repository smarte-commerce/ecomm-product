package com.winnguyen1905.product.persistance.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventories")
public class EInventory extends EBaseAudit {

    @Version
    private int version;

    @Column(name = "inven_stock")
    private int stock;

//    @OneToMany(mappedBy = "inventory", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//    private Set<ReservationEntity> reservations = new HashSet<>();

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "product_id")
    private EVariation productVariation;

//    @ManyToOne
//    @JoinColumn(name = "shop_id")
//    private UserEntity shop;
    
}
