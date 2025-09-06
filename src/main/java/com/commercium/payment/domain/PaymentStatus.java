package com.commercium.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("결제대기"),
    PROCESSING("결제처리중"),
    COMPLETED("결제완료"),
    FAILED("결제실패"),
    CANCELLED("결제취소"),
    PARTIAL_CANCELLED("부분취소");

    private final String description;

    public boolean isCompletable() {
        return this == PENDING || this == PROCESSING;
    }

    public boolean isCancellable() {
        return this == COMPLETED;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}

