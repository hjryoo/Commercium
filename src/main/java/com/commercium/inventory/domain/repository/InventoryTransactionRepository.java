package com.commercium.inventory.domain.repository;

import com.commercium.inventory.domain.InventoryTransaction;
import com.commercium.inventory.domain.ProductId;
import com.commercium.inventory.domain.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository {

    InventoryTransaction save(InventoryTransaction transaction);

    List<InventoryTransaction> saveAll(List<InventoryTransaction> transactions);

    List<InventoryTransaction> findByProductId(ProductId productId);

    List<InventoryTransaction> findByOrderId(String orderId);

    List<InventoryTransaction> findByProductIdAndTransactionType(ProductId productId, TransactionType type);

    List<InventoryTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}