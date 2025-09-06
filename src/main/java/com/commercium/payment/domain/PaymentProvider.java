package com.commercium.payment.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class PaymentProvider {

    private String name;              // PG사 명 (TOSS, KCP, INICIS)
    private String externalPaymentId; // 외부 결제 ID
    private String transactionId;     // 거래 ID

    protected PaymentProvider() {} // JPA용

    public static PaymentProvider of(String name, String externalPaymentId, String transactionId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("결제 대행사명은 필수입니다");
        }
        return new PaymentProvider(name, externalPaymentId, transactionId);
    }

    public PaymentProvider updateTransactionId(String transactionId) {
        return new PaymentProvider(name, externalPaymentId, transactionId);
    }
}

