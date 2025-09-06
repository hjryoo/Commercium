package com.commercium.inventory.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 재고 예약 완료 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleStockReserved(StockReservedEvent event) {
        log.info("재고 예약 완료 이벤트 처리: productId={}, orderId={}",
                event.getProductId(), event.getOrderId());

        try {
            // 알림 서비스로 이벤트 전송
            kafkaTemplate.send("notification.stock-reserved", event.getProductId(), event);

            // 로그 및 통계 서비스로 이벤트 전송
            kafkaTemplate.send("analytics.stock-movement", event.getProductId(), event);

            log.info("재고 예약 완료 이벤트 전송 완료: productId={}", event.getProductId());

        } catch (Exception e) {
            log.error("재고 예약 완료 이벤트 처리 실패: productId={}", event.getProductId(), e);
            throw e;
        }
    }

    /**
     * 재고 해제 완료 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleStockReleased(StockReleasedEvent event) {
        log.info("재고 해제 완료 이벤트 처리: productId={}, orderId={}",
                event.getProductId(), event.getOrderId());

        try {
            // 알림 서비스로 이벤트 전송
            kafkaTemplate.send("notification.stock-released", event.getProductId(), event);

            // 통계 서비스로 이벤트 전송
            kafkaTemplate.send("analytics.stock-movement", event.getProductId(), event);

            log.info("재고 해제 완료 이벤트 전송 완료: productId={}", event.getProductId());

        } catch (Exception e) {
            log.error("재고 해제 완료 이벤트 처리 실패: productId={}", event.getProductId(), e);
            throw e;
        }
    }

    /**
     * 재고 부족 알림 이벤트 처리
     */
    @EventListener
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleStockDepleted(StockDepletedEvent event) {
        log.warn("재고 부족 알림 이벤트 처리: productId={}, 요청수량={}, 가용수량={}",
                event.getProductId(), event.getRequestedQuantity(), event.getAvailableQuantity());

        try {
            // 재고 부족 알림 이벤트 전송
            kafkaTemplate.send("notification.stock-depleted", event.getProductId(), event);

            // 관리자 알림 이벤트 전송
            kafkaTemplate.send("admin.stock-alert", event.getProductId(), event);

            log.info("재고 부족 알림 이벤트 전송 완료: productId={}", event.getProductId());

        } catch (Exception e) {
            log.error("재고 부족 알림 이벤트 처리 실패: productId={}", event.getProductId(), e);
            throw e;
        }
    }
}

