package com.commercium.settlement.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
public class SettlementId {

    private String value;

    protected SettlementId() {} // JPA용

    private SettlementId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("정산 ID는 필수입니다");
        }
        this.value = value;
    }

    public static SettlementId generate() {
        return new SettlementId(UUID.randomUUID().toString());
    }

    public static SettlementId of(String value) {
        return new SettlementId(value);
    }
}
