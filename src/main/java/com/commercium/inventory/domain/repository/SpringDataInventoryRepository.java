package com.commercium.inventory.domain.repository;

import com.commercium.inventory.domain.Inventory;
import com.commercium.inventory.domain.InventoryTransaction;
import com.commercium.inventory.domain.ProductId;
import com.commercium.inventory.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface SpringDataInventoryRepository extends JpaRepository<Inventory, String> {

    Optional<Inventory> findByProductId(ProductId productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") ProductId productId);

    boolean existsByProductId(ProductId productId);
}

interface SpringDataInventoryTransactionRepository extends JpaRepository<InventoryTransaction, String> {

    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(ProductId productId);

    List<InventoryTransaction> findByOrderIdOrderByCreatedAtDesc(String orderId);

    List<InventoryTransaction> findByProductIdAndTransactionTypeOrderByCreatedAtDesc(ProductId productId, TransactionType transactionType);

    List<InventoryTransaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
}

@Repository
@RequiredArgsConstructor
public class JpaInventoryRepository implements InventoryRepository, InventoryTransactionRepository {

    private final SpringDataInventoryRepository inventoryRepository;
    private final SpringDataInventoryTransactionRepository transactionRepository;

    @Override
    public Inventory save(Inventory inventory) {
        Inventory savedInventory = inventoryRepository.save(inventory);

        // 펜딩 트랜잭션들 저장
        List<InventoryTransaction> transactions = savedInventory.getAndClearPendingTransactions();
        if (!transactions.isEmpty()) {
            transactionRepository.saveAll(transactions);
        }

        return savedInventory;
    }

    @Override
    public Optional<Inventory> findByProductId(ProductId productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Override
    public Optional<Inventory> findByProductIdWithLock(ProductId productId) {
        return inventoryRepository.findByProductIdWithLock(productId);
    }

    @Override
    public void delete(Inventory inventory) {
        inventoryRepository.delete(inventory);
    }

    @Override
    public boolean existsByProductId(ProductId productId) {
        return inventoryRepository.existsByProductId(productId);
    }

    // InventoryTransaction 메서드들
    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public List<InventoryTransaction> saveAll(List<InventoryTransaction> transactions) {
        return transactionRepository.saveAll(transactions);
    }

    @Override
    public List<InventoryTransaction> findByProductId(ProductId productId) {
        return transactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public List<InventoryTransaction> findByOrderId(String orderId) {
        return transactionRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    public List<InventoryTransaction> findByProductIdAndTransactionType(ProductId productId, TransactionType type) {
        return transactionRepository.findByProductIdAndTransactionTypeOrderByCreatedAtDesc(productId, type);
    }

    @Override
    public List<InventoryTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }
}
