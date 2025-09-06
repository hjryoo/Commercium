package com.commercium.inventory.service.dto;

import com.commercium.inventory.domain.InventoryTransaction;
import com.commercium.inventory.domain.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StockTransactionResponse {

    private String transactionId;
    private String productId;
    private String orderId;
    private TransactionType transactionType;
    private String transactionDescription;
    private Integer quantity;
    private Integer beforeAvailable;
    private Integer afterAvailable;
    private Integer beforeReserved;
    private Integer afterReserved;
    private String reason;
    private LocalDateTime createdAt;

    public static StockTransactionResponse from(InventoryTransaction transaction) {
        return StockTransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .productId(transaction.getProductId().getValue())
                .orderId(transaction.getOrderId())
                .transactionType(transaction.getTransactionType())
                .transactionDescription(transaction.getTransactionType().getDescription())
                .quantity(transaction.getQuantity())
                .beforeAvailable(transaction.getBeforeAvailable())
                .afterAvailable(transaction.getAfterAvailable())
                .beforeReserved(transaction.getBeforeReserved())
                .afterReserved(transaction.getAfterReserved())
                .reason(transaction.getReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}