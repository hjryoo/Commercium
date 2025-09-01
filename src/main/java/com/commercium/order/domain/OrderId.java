package com.commercium.order.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
public class OrderId {

    private String value;

    protected OrderId() {} // JPA용

    private OrderId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("주문 ID는 필수입니다");
        }
        this.value = value;
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }
}
