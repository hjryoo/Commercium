package com.commercium.order.repository;

import com.commercium.order.domain.Order;
import com.commercium.order.domain.OrderId;
import com.commercium.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface SpringDataOrderRepository extends JpaRepository<Order, OrderId> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByStatusAndCreatedAtBetween(@Param("status") OrderStatus status,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    long countByUserIdAndStatus(String userId, OrderStatus status);
}

@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository repository;

    @Override
    public Order save(Order order) {
        return repository.save(order);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return repository.findById(orderId);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> findByUserId(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Order> findByUserIdAndStatus(String userId, OrderStatus status) {
        return repository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    @Override
    public List<Order> findByStatusAndCreatedAtBetween(OrderStatus status,
                                                       LocalDateTime startDate,
                                                       LocalDateTime endDate) {
        return repository.findByStatusAndCreatedAtBetween(status, startDate, endDate);
    }

    @Override
    public long countByUserIdAndStatus(String userId, OrderStatus status) {
        return repository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public void delete(Order order) {
        repository.delete(order);
    }
}