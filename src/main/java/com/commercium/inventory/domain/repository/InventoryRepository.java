package com.commercium.inventory.domain.repository;

import com.commercium.inventory.domain.Inventory;
import com.commercium.inventory.domain.ProductId;

import java.util.Optional;

public interface InventoryRepository {

    Inventory save(Inventory inventory);

    Optional<Inventory> findByProductId(ProductId productId);

    Optional<Inventory> findByProductIdWithLock(ProductId productId);

    void delete(Inventory inventory);

    boolean existsByProductId(ProductId productId);
}
