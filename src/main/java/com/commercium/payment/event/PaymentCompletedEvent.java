package com.commercium.payment.event;

import com.commercium.common.event.DomainEvent;
import com.commercium.payment.domain.PaymentMethod;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentCompletedEvent extends DomainEvent {

    private final String paymentId;
    private final String orderId;
    private final PaymentMethod paymentMethod;
    private final BigDecimal paidAmount;

    public PaymentCompletedEvent(String paymentId, String orderId,
                                 PaymentMethod paymentMethod, BigDecimal paidAmount) {
        super();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paidAmount = paidAmount;
    }

    @Override
    public String getEventType() {
        return "PaymentCompleted";
    }
}

