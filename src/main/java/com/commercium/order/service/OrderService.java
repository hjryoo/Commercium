package com.commercium.order.service;

import com.commercium.common.exception.BusinessRuleViolationException;
import com.commercium.order.domain.*;
import com.commercium.order.repository.OrderRepository;
import com.commercium.order.service.dto.CreateOrderRequest;
import com.commercium.order.service.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;

    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        log.info("주문 생성 시작: userId={}, itemCount={}", userId, request.getItems().size());

        try {
            // 1. 주문 가능 여부 확인 (도메인 서비스)
            List<OrderItem> orderItems = orderDomainService.validateAndCreateOrderItems(request.getItems());

            // 2. 주문 생성
            Order order = Order.create(
                    userId,
                    orderItems,
                    request.getShippingAddress().toDomain()
            );

            // 3. 저장
            Order savedOrder = orderRepository.save(order);

            log.info("주문 생성 완료: orderId={}, orderNumber={}",
                    savedOrder.getOrderId().getValue(), savedOrder.getOrderNumber());

            return OrderResponse.from(savedOrder);

        } catch (Exception e) {
            log.error("주문 생성 실패: userId={}", userId, e);
            throw new BusinessRuleViolationException("주문 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public void cancelOrder(String userId, String orderId) {
        log.info("주문 취소 시작: userId={}, orderId={}", userId, orderId);

        Order order = findOrderByIdAndUserId(orderId, userId);
        order.cancel();

        orderRepository.save(order);

        log.info("주문 취소 완료: orderId={}", orderId);
    }

    public void confirmPayment(String orderId) {
        log.info("결제 확인 처리: orderId={}", orderId);

        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new BusinessRuleViolationException("주문을 찾을 수 없습니다"));

        order.markAsPaid();
        orderRepository.save(order);

        log.info("결제 확인 완료: orderId={}", orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String userId, String orderId) {
        Order order = findOrderByIdAndUserId(orderId, userId);
        return OrderResponse.from(order);
    }

    private Order findOrderByIdAndUserId(String orderId, String userId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new BusinessRuleViolationException("주문을 찾을 수 없습니다"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("주문에 접근할 권한이 없습니다");
        }

        return order;
    }
}
