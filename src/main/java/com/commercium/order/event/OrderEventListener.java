package com.commercium.order.event;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 주문 생성 이벤트 처리
     * - 재고 차감 요청
     * - 고객 알림 발송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 처리 시작: orderId={}", event.getOrderId());

        try {
            // 1. 재고 차감 이벤트 발행 (Inventory 도메인)
            kafkaTemplate.send("inventory.reserve", event.getOrderId(), event);

            // 2. 고객 알림 이벤트 발행 (Notification 도메인)
            kafkaTemplate.send("notification.order-created", event.getUserId(), event);

            log.info("주문 생성 이벤트 처리 완료: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("주문 생성 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
            throw e; // 재시도를 위해 예외 재발생
        }
    }

    /**
     * 주문 취소 이벤트 처리
     * - 재고 복원 요청
     * - 결제 취소 요청 (결제 완료된 경우)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("주문 취소 이벤트 처리 시작: orderId={}", event.getOrderId());

        try {
            // 1. 재고 복원 이벤트 발행
            kafkaTemplate.send("inventory.restore", event.getOrderId(), event);

            // 2. 결제 취소 이벤트 발행 (필요시)
            kafkaTemplate.send("payment.cancel", event.getOrderId(), event);

            // 3. 고객 알림 이벤트 발행
            kafkaTemplate.send("notification.order-cancelled", event.getOrderId(), event);

            log.info("주문 취소 이벤트 처리 완료: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("주문 취소 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }

    /**
     * 주문 결제 완료 이벤트 처리
     * - 정산 데이터 생성 요청
     * - 배송 준비 알림
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("주문 결제 완료 이벤트 처리 시작: orderId={}", event.getOrderId());

        try {
            // 1. 정산 데이터 생성 이벤트 발행
            kafkaTemplate.send("settlement.create", event.getOrderId(), event);

            // 2. 배송 준비 이벤트 발행
            kafkaTemplate.send("shipping.prepare", event.getOrderId(), event);

            // 3. 고객 알림 이벤트 발행
            kafkaTemplate.send("notification.payment-completed", event.getOrderId(), event);

            log.info("주문 결제 완료 이벤트 처리 완료: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("주문 결제 완료 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }
}