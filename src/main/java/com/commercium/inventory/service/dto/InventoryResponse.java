package com.commercium.inventory.service.dto;

import com.commercium.inventory.domain.Inventory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponse {

    private String inventoryId;
    private String productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
    private LocalDateTime updatedAt;

    public static InventoryResponse from(Inventory inventory) {
        return InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .productId(inventory.getProductId().getValue())
                .availableQuantity(inventory.getStockQuantity().getAvailable())
                .reservedQuantity(inventory.getStockQuantity().getReserved())
                .totalQuantity(inventory.getStockQuantity().getTotalStock())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}

