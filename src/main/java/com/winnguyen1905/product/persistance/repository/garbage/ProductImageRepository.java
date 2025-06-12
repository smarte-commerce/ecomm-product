package com.winnguyen1905.product.persistance.repository.garbage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.garbage.EProductImage;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<EProductImage, UUID> {
    // Page<EProductImage> findByProductId(UUID productId, Pageable pageable);
    // Optional<EProductImage> findByIdAndProductId(UUID id, UUID productId);
}
