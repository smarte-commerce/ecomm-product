package com.winnguyen1905.product.core.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation implements  AbstractModel {
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String orderId;
    private List<ReservationItem> items;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant confirmedAt;
    private Instant cancelledAt;
    private Instant updatedAt;
    private String status; // PENDING, CONFIRMED, EXPIRED, CANCELLED
    
    @Version
    private Long version;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationItem implements AbstractModel {
        private static final long serialVersionUID = 1L;
        
        private UUID productId;
        private UUID variantId;
        private String sku;
        private int quantity;
        private boolean confirmed;
    }
}
