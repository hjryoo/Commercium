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
public class SettlementAmount {

    private BigDecimal totalSales;        // 총 매출
    private BigDecimal commissionAmount;  // 수수료
    private BigDecimal vatAmount;         // 부가세
    private BigDecimal netAmount;         // 정산 금액

    protected SettlementAmount() {} // JPA용

    public static SettlementAmount calculate(BigDecimal totalSales, CommissionRate commissionRate) {
        validateAmount(totalSales);

        BigDecimal commissionAmount = commissionRate.calculateCommission(totalSales);
        BigDecimal vatAmount = calculateVAT(commissionAmount);
        BigDecimal netAmount = totalSales.subtract(commissionAmount).subtract(vatAmount);

        return new SettlementAmount(totalSales, commissionAmount, vatAmount, netAmount);
    }

    public static SettlementAmount zero() {
        return new SettlementAmount(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public SettlementAmount add(SettlementAmount other) {
        return new SettlementAmount(
                totalSales.add(other.totalSales),
                commissionAmount.add(other.commissionAmount),
                vatAmount.add(other.vatAmount),
                netAmount.add(other.netAmount)
        );
    }

    public BigDecimal getCommissionRate() {
        if (totalSales.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return commissionAmount.divide(totalSales, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateVAT(BigDecimal commissionAmount) {
        // 수수료에 대한 10% 부가세 계산
        return commissionAmount.multiply(BigDecimal.valueOf(0.1))
                .setScale(0, RoundingMode.HALF_UP);
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("매출 금액은 0 이상이어야 합니다");
        }
    }
}
