package com.commercium.order.event;

import com.commercium.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderCreatedEvent extends DomainEvent {

    private final String orderId;
    private final String userId;
    private final BigDecimal totalAmount;

    public OrderCreatedEvent(String orderId, String userId, BigDecimal totalAmount) {
        super();
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
    }

    @Override
    public String getEventType() {
        return "OrderCreated";
    }
}
