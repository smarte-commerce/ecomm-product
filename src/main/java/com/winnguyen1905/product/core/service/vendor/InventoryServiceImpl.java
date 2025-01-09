package com.winnguyen1905.product.core.service.vendor;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

  // private final RedisTemplate<String, Integer> redisTemplate;
  // private final InventoryRepository inventoryRepository;
  // private final String INVENTORY_KEY = "inventory:";

  @Override
  @SuppressWarnings({ "null", "unchecked", "rawtypes" })
  public Boolean isAccessStock(EInventory inventory, Integer quantity) {
    // String key = this.INVENTORY_KEY + inventory.getId();

    // if (redisTemplate.opsForValue().get(key) == null)
    //   redisTemplate.opsForValue().set(key, inventory.getStock(), Duration.ofSeconds(60));

    // SessionCallback<List<Object>> sessionCallback = new SessionCallback<List<Object>>() {
    //   @Nullable
    //   @Override
    //   public List<Object> execute(RedisOperations operations) throws DataAccessException {
    //     Integer stock = (Integer) operations.opsForValue().get(key);

    //     if (stock < quantity)
    //       return null;

    //     operations.watch(key);
    //     operations.multi();
    //     operations.opsForValue().set(key, stock - quantity);
    //     return operations.exec();
    //   }
    // };

    // SessionCallback<List<Object>> result = sessionCallback;
    // return result != null;
    return null;
  }

  @Override
  public Boolean handleUpdateInventoryForReservation(UUID inventoryId, UUID customerId, Integer quantity) {
    // UserEntity user = this.userRepository.findById(customerId)
    //     .orElseThrow(() -> new CustomRuntimeException("Not found user id " + customerId));
    // InventoryEntity inventory = this.inventoryRepository.findById(inventoryId)
    //     .orElseThrow(() -> new CustomRuntimeException("Not found inventory id " + inventoryId));

    // if (!isAccessStock(inventory, quantity))
    //   return false;

    // inventory.setStock(inventory.getStock() - quantity);
    // ReservationEntity reservation = new ReservationEntity();
    // reservation.setCustomer(user);
    // reservation.setInventory(inventory);
    // inventory.getReservations().add(reservation);
    // this.inventoryRepository.save(inventory);

    return true;
  }

}
