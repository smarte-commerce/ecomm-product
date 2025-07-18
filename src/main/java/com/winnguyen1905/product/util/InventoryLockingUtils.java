package com.winnguyen1905.product.util;

import com.winnguyen1905.product.exception.InventoryException;
import com.winnguyen1905.product.exception.OptimisticLockingException;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class InventoryLockingUtils {
    
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(100);
    
    private final InventoryRepository inventoryRepository;
    
    public InventoryLockingUtils(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    /**
     * Execute a function with optimistic locking and retry on failure
     * 
     * @param id The ID of the inventory to operate on
     * @param updateFunction A function that updates the inventory entity
     * @return A Mono containing the updated inventory entity
     */
    public Mono<EInventory> executeWithOptimisticLock(UUID id, Function<EInventory, EInventory> updateFunction) {
        return Mono.fromCallable(() -> {
            try {
                return doInTransaction(id, updateFunction);
            } catch (OptimisticLockingFailureException e) {
                log.warn("Optimistic lock failed for inventory {}, will retry", id);
                throw e;
            }
        })
        .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_BACKOFF)
            .filter(throwable -> throwable instanceof OptimisticLockingFailureException)
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                new OptimisticLockingException("Failed to update inventory after " + MAX_RETRIES + " retries", 
                    retrySignal.failure())));
    }
    
    /**
     * Execute a function with optimistic locking using SKU and retry on failure
     * 
     * @param sku The SKU of the inventory to operate on
     * @param updateFunction A function that updates the inventory entity
     * @return A Mono containing the updated inventory entity
     */
    public Mono<EInventory> executeWithOptimisticLockBySku(String sku, Function<EInventory, EInventory> updateFunction) {
        return Mono.fromCallable(() -> {
            try {
                return doInTransactionBySku(sku, updateFunction);
            } catch (OptimisticLockingFailureException e) {
                log.warn("Optimistic lock failed for inventory SKU {}, will retry", sku);
                throw e;
            }
        })
        .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_BACKOFF)
            .filter(throwable -> throwable instanceof OptimisticLockingFailureException)
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                new OptimisticLockingException("Failed to update inventory for SKU " + sku + " after " + MAX_RETRIES + " retries", 
                    retrySignal.failure())));
    }
    
    /**
     * Execute a function with optimistic locking that returns a boolean result
     * 
     * @param id The ID of the inventory to operate on
     * @param updateFunction A function that updates the inventory entity
     * @return A Mono containing true if successful, false otherwise
     */
    public Mono<Boolean> executeWithOptimisticLockReturningBoolean(UUID id, Function<EInventory, EInventory> updateFunction) {
        return executeWithOptimisticLock(id, updateFunction)
            .map(inventory -> true)
            .onErrorResume(e -> {
                log.error("Failed to update inventory ID {}: {}", id, e.getMessage());
                return Mono.just(false);
            });
    }
    
    /**
     * Execute a function with optimistic locking using SKU that returns a boolean result
     * 
     * @param sku The SKU of the inventory to operate on
     * @param updateFunction A function that updates the inventory entity
     * @return A Mono containing true if successful, false otherwise
     */
    public Mono<Boolean> executeWithOptimisticLockBySkuReturningBoolean(String sku, Function<EInventory, EInventory> updateFunction) {
        return executeWithOptimisticLockBySku(sku, updateFunction)
            .map(inventory -> true)
            .onErrorResume(e -> {
                log.error("Failed to update inventory SKU {}: {}", sku, e.getMessage());
                return Mono.just(false);
            });
    }
    
    @Transactional
    protected EInventory doInTransaction(UUID id, Function<EInventory, EInventory> updateFunction) {
        EInventory inventory = inventoryRepository.findByIdWithOptimisticLock(id)
                .orElseThrow(() -> new InventoryException("Inventory not found with id: " + id));
        
        inventory = updateFunction.apply(inventory);
        return inventoryRepository.save(inventory);
    }
    
    @Transactional
    protected EInventory doInTransactionBySku(String sku, Function<EInventory, EInventory> updateFunction) {
        EInventory inventory = inventoryRepository.findBySkuWithOptimisticLock(sku)
                .orElseThrow(() -> new InventoryException("Inventory not found with SKU: " + sku));
        
        inventory = updateFunction.apply(inventory);
        return inventoryRepository.save(inventory);
    }
} 
