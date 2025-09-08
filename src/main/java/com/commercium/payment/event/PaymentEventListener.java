package com.commercium.payment.event;

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
public class PaymentEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 결제 완료 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 처리: paymentId={}, orderId={}",
                event.getPaymentId(), event.getOrderId());

        try {
            // 주문 서비스로 결제 완료 이벤트 전송
            kafkaTemplate.send("order.payment-completed", event.getOrderId(), event);

            // 재고 서비스로 재고 차감 이벤트 전송
            kafkaTemplate.send("inventory.decrease", event.getOrderId(), event);

            // 정산 서비스로 정산 데이터 생성 이벤트 전송
            kafkaTemplate.send("settlement.create", event.getOrderId(), event);

            // 알림 서비스로 결제 완료 알림 이벤트 전송
            kafkaTemplate.send("notification.payment-completed", event.getOrderId(), event);

            log.info("결제 완료 이벤트 처리 완료: paymentId={}", event.getPaymentId());

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: paymentId={}", event.getPaymentId(), e);
            throw e;
        }
    }

    /**
     * 결제 실패 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 처리: paymentId={}, orderId={}, reason={}",
                event.getPaymentId(), event.getOrderId(), event.getFailureReason());

        try {
            // 주문 서비스로 결제 실패 이벤트 전송 (주문 상태 원복)
            kafkaTemplate.send("order.payment-failed", event.getOrderId(), event);

            // 재고 서비스로 예약 해제 이벤트 전송
            kafkaTemplate.send("inventory.restore", event.getOrderId(), event);

            // 알림 서비스로 결제 실패 알림 이벤트 전송
            kafkaTemplate.send("notification.payment-failed", event.getOrderId(), event);

            log.info("결제 실패 이벤트 처리 완료: paymentId={}", event.getPaymentId());

        } catch (Exception e) {
            log.error("결제 실패 이벤트 처리 실패: paymentId={}", event.getPaymentId(), e);
            throw e;
        }
    }

    /**
     * 결제 취소 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        log.info("결제 취소 이벤트 처리: paymentId={}, orderId={}, amount={}",
                event.getPaymentId(), event.getOrderId(), event.getCancelledAmount());

        try {
            // 주문 서비스로 결제 취소 이벤트 전송
            kafkaTemplate.send("order.payment-cancelled", event.getOrderId(), event);

            // 정산 서비스로 취소 데이터 생성 이벤트 전송
            kafkaTemplate.send("settlement.cancel", event.getOrderId(), event);

            // 알림 서비스로 결제 취소 알림 이벤트 전송
            kafkaTemplate.send("notification.payment-cancelled", event.getOrderId(), event);

            log.info("결제 취소 이벤트 처리 완료: paymentId={}", event.getPaymentId());

        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 실패: paymentId={}", event.getPaymentId(), e);
            throw e;
        }
    }
}
