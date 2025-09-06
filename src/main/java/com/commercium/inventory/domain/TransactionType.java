package com.commercium.inventory.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    INCREASE("입고"),           // 재고 증가
    DECREASE("출고"),           // 재고 감소 (실제 판매)
    RESERVE("예약"),            // 재고 예약 (주문 시)
    RELEASE("해제"),            // 예약 해제 (주문 취소 시)
    ADJUSTMENT("조정");         // 재고 조정 (관리자)

    private final String description;
}