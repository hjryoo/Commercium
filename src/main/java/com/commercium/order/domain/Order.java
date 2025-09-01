package com.commercium.order.domain;

import com.commercium.common.event.DomainEvents;
import com.commercium.order.event.OrderCancelledEvent;
import com.commercium.order.event.OrderCreatedEvent;
import com.commercium.order.event.OrderPaidEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @EmbeddedId
    private OrderId orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Embedded
    private ShippingAddress shippingAddress;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Order(String userId, List<OrderItem> orderItems, ShippingAddress shippingAddress) {
        this.orderId = OrderId.generate();
        this.userId = userId;
        this.orderNumber = generateOrderNumber();
        this.status = OrderStatus.PLACED;
        this.orderItems.addAll(orderItems);
        this.shippingAddress = shippingAddress;
        this.totalAmount = calculateTotalAmount();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        DomainEvents.raise(new OrderCreatedEvent(this.orderId.getValue(), this.userId, this.totalAmount));
    }

    public static Order create(String userId, List<OrderItem> orderItems, ShippingAddress shippingAddress) {
        validateOrderCreation(userId, orderItems);
        return new Order(userId, orderItems, shippingAddress);
    }

    public void cancel() {
        if (!status.canCancel()) {
            throw new IllegalStateException(
                    String.format("주문 상태가 %s인 경우 취소할 수 없습니다", status.getDescription())
            );
        }

        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        DomainEvents.raise(new OrderCancelledEvent(this.orderId.getValue(), this.orderItems));
    }

    public void markAsPaid() {
        if (!status.canPay()) {
            throw new IllegalStateException(
                    String.format("주문 상태가 %s인 경우 결제 완료 처리할 수 없습니다", status.getDescription())
            );
        }

        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        DomainEvents.raise(new OrderPaidEvent(this.orderId.getValue(), this.orderItems));
    }

    public void ship() {
        if (!status.canShip()) {
            throw new IllegalStateException(
                    String.format("주문 상태가 %s인 경우 배송 처리할 수 없습니다", status.getDescription())
            );
        }

        this.status = OrderStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    private BigDecimal calculateTotalAmount() {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() +
                String.valueOf((int)(Math.random() * 1000)).substring(0, 3);
    }

    private static void validateOrderCreation(String userId, List<OrderItem> orderItems) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문 상품은 최소 1개 이상이어야 합니다");
        }
    }
}