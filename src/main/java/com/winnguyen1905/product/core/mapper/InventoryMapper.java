package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;

import com.winnguyen1905.product.core.model.Inventory;
import com.winnguyen1905.product.persistance.elasticsearch.ESInventory;
import com.winnguyen1905.product.persistance.entity.EInventory;

import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMapper {
  Inventory toInventory(ESInventory inventory);
  ESInventory toESInventory(EInventory inventory);
  Inventory toInventory(EInventory inventory);
}
