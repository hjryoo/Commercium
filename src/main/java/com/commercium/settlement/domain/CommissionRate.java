package com.commercium.settlement.domain;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class CommissionRate {

    private BigDecimal rate; // 수수료율 (예: 0.03 = 3%)

    protected CommissionRate() {} // JPA용

    public static CommissionRate of(BigDecimal rate) {
        validateRate(rate);
        return new CommissionRate(rate);
    }

    public static CommissionRate of(double rate) {
        return of(BigDecimal.valueOf(rate));
    }

    public static CommissionRate defaultRate() {
        return of(0.03); // 기본 3% 수수료
    }

    public BigDecimal calculateCommission(BigDecimal amount) {
        return amount.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getPercentage() {
        return rate.multiply(BigDecimal.valueOf(100));
    }

    private static void validateRate(BigDecimal rate) {
        if (rate == null) {
            throw new IllegalArgumentException("수수료율은 필수입니다");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("수수료율은 0~100% 사이여야 합니다");
        }
    }
}
