package com.winnguyen1905.product.core.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.InventoryService;
import com.winnguyen1905.product.exception.BadRequestException;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
  private final InventoryRepository inventoryRepository;

  // private final RedisTemplate<String, Integer> redisTemplate;
  // private final InventoryRepository inventoryRepository;
  // private final String INVENTORY_KEY = "inventory:";

  @Override
  @SuppressWarnings({ "null", "unchecked", "rawtypes" })
  public Boolean isAccessStock(EInventory inventory, Integer quantity) {
    // String key = this.INVENTORY_KEY + inventory.getId();

    // if (redisTemplate.opsForValue().get(key) == null)
    // redisTemplate.opsForValue().set(key, inventory.getStock(),
    // Duration.ofSeconds(60));

    // SessionCallback<List<Object>> sessionCallback = new
    // SessionCallback<List<Object>>() {
    // @Nullable
    // @Override
    // public List<Object> execute(RedisOperations operations) throws
    // DataAccessException {
    // Integer stock = (Integer) operations.opsForValue().get(key);

    // if (stock < quantity)
    // return null;

    // operations.watch(key);
    // operations.multi();
    // operations.opsForValue().set(key, stock - quantity);
    // return operations.exec();
    // }
    // };

    // SessionCallback<List<Object>> result = sessionCallback;
    // return result != null;
    return null;
  }

  // @Override
  // public Boolean handleUpdateInventoryForReservation(UUID inventoryId, UUID
  // customerId, Integer quantity) {
  // UserEntity user = this.userRepository.findById(customerId)
  // .orElseThrow(() -> new CustomRuntimeException("Not found user id " +
  // customerId));
  // InventoryEntity inventory = this.inventoryRepository.findById(inventoryId)
  // .orElseThrow(() -> new CustomRuntimeException("Not found inventory id " +
  // inventoryId));

  // if (!isAccessStock(inventory, quantity))
  // return false;

  // inventory.setStock(inventory.getStock() - quantity);
  // ReservationEntity reservation = new ReservationEntity();
  // reservation.setCustomer(user);
  // reservation.setInventory(inventory);
  // inventory.getReservations().add(reservation);
  // this.inventoryRepository.save(inventory);

  // return true;
  // }

  @Override
  public PagedResponse<InventoryVm> getProductInventory(UUID productId, Pageable pageable) {
    var page = inventoryRepository.findAll(pageable);
    var inventories = page.getContent().stream()
        .map(this::mapToInventoryVm)
        .collect(Collectors.toList());

    return PagedResponse.<InventoryVm>builder()
        .results(inventories)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .build();
  }

  @Override
  public InventoryVm getInventoryById(UUID inventoryId) {
    return inventoryRepository.findById(inventoryId)
        .map(this::mapToInventoryVm)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));
  }

  @Override
  public InventoryVm getInventoryBySku(String sku) {
    return inventoryRepository.findBySku(sku)
        .map(this::mapToInventoryVm)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with sku: " + sku));
  }

  @Override
  public InventoryVm updateInventory(UUID inventoryId) {
    var inventory = inventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

    return mapToInventoryVm(inventoryRepository.save(inventory));
  }

  @Override
  public InventoryVm reserveInventory(UUID inventoryId, Integer quantity) {
    var inventory = inventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

    if (inventory.getQuantityAvailable() < quantity) {
      throw new BadRequestException("Insufficient inventory available");
    }

    inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
    inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);

    return mapToInventoryVm(inventoryRepository.save(inventory));
  }

  @Override
  public InventoryVm releaseInventory(UUID inventoryId, Integer quantity) {
    var inventory = inventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

    if (inventory.getQuantityReserved() < quantity) {
      throw new BadRequestException("Cannot release more than reserved quantity");
    }

    inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
    inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);

    return mapToInventoryVm(inventoryRepository.save(inventory));
  }

  private InventoryVm mapToInventoryVm(EInventory inventory) {
    return InventoryVm.builder()
        .id(inventory.getId())
        .productId(inventory.getProduct().getId().toString())
        .sku(inventory.getSku())
        .quantityAvailable(inventory.getQuantityAvailable())
        .quantityReserved(inventory.getQuantityReserved())
        .quantitySold(inventory.getQuantitySold())
        .address(inventory.getAddress())  
        .build();
  }
}
