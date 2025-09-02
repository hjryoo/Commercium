package com.commercium.inventory.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryTransaction {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_id"))
    private ProductId productId;

    @Column(name = "order_id")
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "before_available")
    private Integer beforeAvailable;

    @Column(name = "after_available")
    private Integer afterAvailable;

    @Column(name = "before_reserved")
    private Integer beforeReserved;

    @Column(name = "after_reserved")
    private Integer afterReserved;

    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private InventoryTransaction(ProductId productId, String orderId, TransactionType transactionType,
                                 Integer quantity, StockQuantity beforeStock, StockQuantity afterStock, String reason) {
        this.transactionId = UUID.randomUUID().toString();
        this.productId = productId;
        this.orderId = orderId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.beforeAvailable = beforeStock.getAvailable();
        this.beforeReserved = beforeStock.getReserved();
        this.afterAvailable = afterStock.getAvailable();
        this.afterReserved = afterStock.getReserved();
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public static InventoryTransaction create(ProductId productId, String orderId, TransactionType transactionType,
                                              Integer quantity, StockQuantity beforeStock, StockQuantity afterStock, String reason) {
        return new InventoryTransaction(productId, orderId, transactionType, quantity, beforeStock, afterStock, reason);
    }
}
