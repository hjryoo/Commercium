package com.commercium.order.repository;

import com.commercium.order.domain.Order;
import com.commercium.order.domain.OrderId;
import com.commercium.order.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(String userId);

    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);

    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status,
                                                LocalDateTime startDate,
                                                LocalDateTime endDate);

    long countByUserIdAndStatus(String userId, OrderStatus status);

    void delete(Order order);
}

