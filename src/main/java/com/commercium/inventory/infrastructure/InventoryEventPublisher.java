package com.commercium.inventory.infrastructure;

import com.commercium.inventory.event.StockDepletedEvent;
import com.commercium.inventory.event.StockReleasedEvent;
import com.commercium.inventory.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishStockReservedEvent(StockReservedEvent event) {
        publishEvent("inventory.stock-reserved", event.getProductId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishStockReleasedEvent(StockReleasedEvent event) {
        publishEvent("inventory.stock-released", event.getProductId(), event);
    }

    @EventListener
    public void publishStockDepletedEvent(StockDepletedEvent event) {
        publishEvent("inventory.stock-depleted", event.getProductId(), event);
    }

    private void publishEvent(String topic, String key, Object event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("이벤트 발행 실패: topic={}, key={}, event={}", topic, key, event.getClass().getSimpleName(), ex);
                } else {
                    log.debug("이벤트 발행 성공: topic={}, key={}, partition={}, offset={}",
                            topic, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("이벤트 발행 중 예외 발생: topic={}, key={}", topic, key, e);
        }
    }
}
