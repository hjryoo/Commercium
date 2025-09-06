package com.commercium.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("신용카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE("휴대폰"),
    POINT("포인트");

    private final String description;

    public boolean requiresCallback() {
        return this == VIRTUAL_ACCOUNT || this == BANK_TRANSFER;
    }

    public boolean isRealTime() {
        return this == CARD || this == MOBILE;
    }
}