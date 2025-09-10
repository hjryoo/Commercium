package com.commercium.settlement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
    PENDING("정산대기"),
    CALCULATING("정산계산중"),
    COMPLETED("정산완료"),
    FAILED("정산실패"),
    CANCELLED("정산취소");

    private final String description;

    public boolean isCompletable() {
        return this == PENDING || this == CALCULATING;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}