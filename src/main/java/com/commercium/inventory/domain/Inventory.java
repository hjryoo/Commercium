package com.commercium.inventory.domain;

import com.commercium.common.event.DomainEvents;
import com.commercium.inventory.event.StockDepletedEvent;
import com.commercium.inventory.event.StockReleasedEvent;
import com.commercium.inventory.event.StockReservedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id
    @Column(name = "inventory_id")
    private String inventoryId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_id"))
    private ProductId productId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "available", column = @Column(name = "quantity")),
            @AttributeOverride(name = "reserved", column = @Column(name = "reserved_quantity"))
    })
    private StockQuantity stockQuantity;

    @Version
    private Integer version; // 낙관적 락

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private List<InventoryTransaction> pendingTransactions = new ArrayList<>();

    private Inventory(ProductId productId, Integer initialQuantity) {
        this.inventoryId = java.util.UUID.randomUUID().toString();
        this.productId = productId;
        this.stockQuantity = StockQuantity.initialStock(initialQuantity);
        this.version = 1;
        this.updatedAt = LocalDateTime.now();
    }

    public static Inventory create(ProductId productId, Integer initialQuantity) {
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("초기 재고는 0 이상이어야 합니다");
        }
        return new Inventory(productId, initialQuantity);
    }

    /**
     * 재고 예약 (주문 시)
     */
    public void reserve(String orderId, Integer quantity, String reason) {
        StockQuantity beforeStock = this.stockQuantity;

        if (!beforeStock.canReserve(quantity)) {
            // 재고 부족 이벤트 발행
            DomainEvents.raise(new StockDepletedEvent(
                    productId.getValue(),
                    quantity,
                    beforeStock.getAvailable()
            ));
            throw new IllegalStateException("재고가 부족합니다");
        }

        this.stockQuantity = beforeStock.reserve(quantity);
        this.updatedAt = LocalDateTime.now();

        // 트랜잭션 이력 생성
        addPendingTransaction(orderId, TransactionType.RESERVE, quantity, beforeStock, reason);

        // 재고 예약 완료 이벤트 발행
        DomainEvents.raise(new StockReservedEvent(
                productId.getValue(),
                orderId,
                quantity
        ));
    }

    /**
     * 예약 해제 (주문 취소 시)
     */
    public void releaseReservation(String orderId, Integer quantity, String reason) {
        StockQuantity beforeStock = this.stockQuantity;
        this.stockQuantity = beforeStock.release(quantity);
        this.updatedAt = LocalDateTime.now();

        // 트랜잭션 이력 생성
        addPendingTransaction(orderId, TransactionType.RELEASE, quantity, beforeStock, reason);

        // 재고 해제 완료 이벤트 발행
        DomainEvents.raise(new StockReleasedEvent(
                productId.getValue(),
                orderId,
                quantity
        ));
    }

    /**
     * 재고 차감 (결제 완료 시)
     */
    public void decrease(String orderId, Integer quantity, String reason) {
        StockQuantity beforeStock = this.stockQuantity;
        this.stockQuantity = beforeStock.decrease(quantity);
        this.updatedAt = LocalDateTime.now();

        // 트랜잭션 이력 생성
        addPendingTransaction(orderId, TransactionType.DECREASE, quantity, beforeStock, reason);
    }

    /**
     * 재고 증가 (입고, 반품 등)
     */
    public void increase(Integer quantity, String reason) {
        StockQuantity beforeStock = this.stockQuantity;
        this.stockQuantity = beforeStock.increase(quantity);
        this.updatedAt = LocalDateTime.now();

        // 트랜잭션 이력 생성
        addPendingTransaction(null, TransactionType.INCREASE, quantity, beforeStock, reason);
    }

    /**
     * 관리자 재고 조정
     */
    public void adjust(Integer newQuantity, String reason) {
        StockQuantity beforeStock = this.stockQuantity;
        Integer adjustmentQuantity = newQuantity - beforeStock.getTotalStock();

        this.stockQuantity = StockQuantity.of(newQuantity, 0); // 조정 시 예약 재고는 모두 해제
        this.updatedAt = LocalDateTime.now();

        // 트랜잭션 이력 생성
        addPendingTransaction(null, TransactionType.ADJUSTMENT, adjustmentQuantity, beforeStock, reason);
    }

    public boolean isStockSufficient(Integer requiredQuantity) {
        return stockQuantity.canReserve(requiredQuantity);
    }

    public List<InventoryTransaction> getAndClearPendingTransactions() {
        List<InventoryTransaction> transactions = new ArrayList<>(pendingTransactions);
        pendingTransactions.clear();
        return transactions;
    }

    private void addPendingTransaction(String orderId, TransactionType type, Integer quantity,
                                       StockQuantity beforeStock, String reason) {
        InventoryTransaction transaction = InventoryTransaction.create(
                productId, orderId, type, quantity, beforeStock, stockQuantity, reason
        );
        pendingTransactions.add(transaction);
    }
}