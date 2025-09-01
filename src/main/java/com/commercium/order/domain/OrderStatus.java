package com.commercium.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PLACED("주문완료"),
    PAID("결제완료"),
    PREPARING("상품준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("주문취소");

    private final String description;

    public boolean canCancel() {
        return this == PLACED || this == PAID;
    }

    public boolean canPay() {
        return this == PLACED;
    }

    public boolean canShip() {
        return this == PAID || this == PREPARING;
    }
}