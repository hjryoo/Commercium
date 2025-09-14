package com.commercium.settlement.event;

import com.commercium.payment.event.PaymentCompletedEvent;
import com.commercium.settlement.service.SettlementService;
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
public class PaymentEventConsumer {

    private final SettlementService settlementService;

    /**
     * 결제 완료 이벤트 수신 - 정산 데이터 생성
     */
    @KafkaListener(topics = "settlement.create", groupId = "settlement-group")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void handlePaymentCompleted(@Payload PaymentCompletedEvent event,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       Acknowledgment ack) {

        log.info("결제 완료 이벤트 수신: orderId={}, paymentId={}, amount={}",
                event.getOrderId(), event.getPaymentId(), event.getPaidAmount());

        try {
            // 실시간 정산 데이터 생성은 하지 않고, 배치에서 처리
            // 여기서는 정산 관련 메타데이터만 저장하거나 캐시 갱신
            log.info("결제 완료 이벤트 처리 완료: orderId={}", event.getOrderId());

            ack.acknowledge();

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }
}