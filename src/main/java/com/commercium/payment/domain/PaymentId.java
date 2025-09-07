package com.commercium.payment.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
public class PaymentId {

    private String value;

    protected PaymentId() {} // JPA용

    private PaymentId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("결제 ID는 필수입니다");
        }
        this.value = value;
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID().toString());
    }

    public static PaymentId of(String value) {
        return new PaymentId(value);
    }
}

