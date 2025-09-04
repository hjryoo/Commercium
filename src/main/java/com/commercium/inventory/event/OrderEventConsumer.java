package com.commercium.inventory.event;

import com.commercium.inventory.service.StockReservationService;
import com.commercium.order.event.OrderCancelledEvent;
import com.commercium.order.event.OrderCreatedEvent;
import com.commercium.order.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final StockReservationService stockReservationService;

    /**
     * 주문 생성 이벤트 수신 - 재고 예약
     */
    @KafkaListener(topics = "inventory.reserve", groupId = "inventory-group")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void handleOrderCreated(@Payload OrderCreatedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment ack) {

        log.info("주문 생성 이벤트 수신: orderId={}, topic={}", event.getOrderId(), topic);

        try {
            // 주문의 각 상품에 대해 재고 예약 처리
            // 실제로는 OrderItem 정보를 통해 각 상품별로 예약
            // 여기서는 단순화해서 처리
            String productId = extractProductIdFromOrder(event.getOrderId()); // Mock
            Integer quantity = 1; // Mock

            stockReservationService.reserveStock(productId, event.getOrderId(), quantity);

            ack.acknowledge(); // 수동 커밋
            log.info("재고 예약 처리 완료: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("재고 예약 처리 실패: orderId={}", event.getOrderId(), e);
            throw e; // 재시도를 위해 예외 재발생
        }
    }

    /**
     * 주문 취소 이벤트 수신 - 예약 해제
     */
    @KafkaListener(topics = "inventory.restore", groupId = "inventory-group")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void handleOrderCancelled(@Payload OrderCancelledEvent event,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     Acknowledgment ack) {

        log.info("주문 취소 이벤트 수신: orderId={}, topic={}", event.getOrderId(), topic);

        try {
            // 각 주문 상품에 대해 예약 해제 처리
            event.getOrderItems().forEach(orderItem -> {
                stockReservationService.releaseReservation(
                        orderItem.getProductId(),
                        event.getOrderId(),
                        orderItem.getQuantity()
                );
            });

            ack.acknowledge();
            log.info("재고 예약 해제 처리 완료: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("재고 예약 해제 처리 실패: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }

    /**
     * 주문 결제 완료 이벤트 수신 - 재고 차감
     */
    @KafkaListener(topics = "inventory.decrease", groupId = "inventory-group")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void handleOrderPaid(@Payload OrderPaidEvent event,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                Acknowledgment ack) {

        log.info("주문 결제 완료 이벤트 수신: orderId={}, topic={}", event.getOrderId(), topic);

        try {
            // 각 주문 상품에 대해 실제 재고 차감
            event.getOrderItems().forEach(orderItem -> {
                stockReservationService.decreaseStock(
                        orderItem.getProductId(),
                        event.getOrderId(),
                        orderItem.getQuantity()
                );
            });

            ack.acknowledge();
            log.info("재고 차감 처리 완료: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("재고 차감 처리 실패: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }

    private String extractProductIdFromOrder(String orderId) {
        // Mock: 실제로는 Order 도메인에서 상품 정보 조회
        return "product-" + orderId.substring(0, 3);
    }
}