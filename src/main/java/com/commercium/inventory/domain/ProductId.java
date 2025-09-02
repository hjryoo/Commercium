package com.commercium.inventory.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
public class ProductId {

    private String value;

    protected ProductId() {} // JPA용

    private ProductId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }
        this.value = value;
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }
}
