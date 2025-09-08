package com.commercium.payment.event;

import com.commercium.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class PaymentFailedEvent extends DomainEvent {

    private final String paymentId;
    private final String orderId;
    private final String failureReason;

    public PaymentFailedEvent(String paymentId, String orderId, String failureReason) {
        super();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.failureReason = failureReason;
    }

    @Override
    public String getEventType() {
        return "PaymentFailed";
    }
}
