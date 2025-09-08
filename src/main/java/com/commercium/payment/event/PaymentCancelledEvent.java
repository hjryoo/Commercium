package com.commercium.payment.event;

import com.commercium.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentCancelledEvent extends DomainEvent {

    private final String paymentId;
    private final String orderId;
    private final BigDecimal cancelledAmount;
    private final String cancelReason;

    public PaymentCancelledEvent(String paymentId, String orderId,
                                 BigDecimal cancelledAmount, String cancelReason) {
        super();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.cancelledAmount = cancelledAmount;
        this.cancelReason = cancelReason;
    }

    @Override
    public String getEventType() {
        return "PaymentCancelled";
    }
}
