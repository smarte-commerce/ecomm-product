package com.winnguyen1905.product.core.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reservation implements AbstractModel {
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String status;
    private String orderId;
    private String userId;
    private List<ReservationItem> items = new ArrayList<>();
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
    private Instant confirmedAt;
    private Instant cancelledAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationItem implements AbstractModel {
        private static final long serialVersionUID = 1L;
        
        private String sku;
        private Integer quantity;
        private UUID inventoryId;
        private UUID productId;
        private UUID variantId;
    }
}
