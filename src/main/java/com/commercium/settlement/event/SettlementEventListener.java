package com.commercium.settlement.event;
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
public class SettlementEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 정산 생성 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleSettlementCreated(SettlementCreatedEvent event) {
        log.info("정산 생성 이벤트 처리: settlementId={}, sellerId={}, period={}",
                event.getSettlementId(), event.getSellerId(), event.getPeriod().getDescription());

        try {
            // 알림 서비스로 정산 생성 알림 이벤트 전송
            kafkaTemplate.send("notification.settlement-created", event.getSellerId(), event);

            // 통계 서비스로 정산 생성 이벤트 전송
            kafkaTemplate.send("analytics.settlement-created", event.getSellerId(), event);

            log.info("정산 생성 이벤트 처리 완료: settlementId={}", event.getSettlementId());

        } catch (Exception e) {
            log.error("정산 생성 이벤트 처리 실패: settlementId={}", event.getSettlementId(), e);
            throw e;
        }
    }

    /**
     * 정산 완료 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleSettlementCompleted(SettlementCompletedEvent event) {
        log.info("정산 완료 이벤트 처리: settlementId={}, sellerId={}, netAmount={}",
                event.getSettlementId(), event.getSellerId(), event.getNetAmount());

        try {
            // 알림 서비스로 정산 완료 알림 이벤트 전송
            kafkaTemplate.send("notification.settlement-completed", event.getSellerId(), event);

            // 통계 서비스로 정산 완료 이벤트 전송
            kafkaTemplate.send("analytics.settlement-completed", event.getSellerId(), event);

            // 회계 시스템으로 정산 완료 이벤트 전송
            kafkaTemplate.send("accounting.settlement-completed", event.getSellerId(), event);

            log.info("정산 완료 이벤트 처리 완료: settlementId={}", event.getSettlementId());

        } catch (Exception e) {
            log.error("정산 완료 이벤트 처리 실패: settlementId={}", event.getSettlementId(), e);
            throw e;
        }
    }
}

