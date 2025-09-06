package com.commercium.payment.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class PaymentAmount {

    private BigDecimal totalAmount;      // 총 결제 금액
    private BigDecimal paidAmount;       // 실제 결제된 금액
    private BigDecimal cancelledAmount;  // 취소된 금액

    protected PaymentAmount() {} // JPA용

    public static PaymentAmount of(BigDecimal totalAmount) {
        validateAmount(totalAmount);
        return new PaymentAmount(totalAmount, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public PaymentAmount markAsPaid(BigDecimal amount) {
        validateAmount(amount);

        if (amount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("결제 금액이 총 금액을 초과할 수 없습니다");
        }

        return new PaymentAmount(totalAmount, amount, cancelledAmount);
    }

    public PaymentAmount cancel(BigDecimal cancelAmount) {
        validateAmount(cancelAmount);

        BigDecimal newCancelledAmount = cancelledAmount.add(cancelAmount);

        if (newCancelledAmount.compareTo(paidAmount) > 0) {
            throw new IllegalArgumentException("취소 금액이 결제 금액을 초과할 수 없습니다");
        }

        return new PaymentAmount(totalAmount, paidAmount, newCancelledAmount);
    }

    public BigDecimal getRefundableAmount() {
        return paidAmount.subtract(cancelledAmount);
    }

    public boolean isFullyPaid() {
        return paidAmount.compareTo(totalAmount) == 0;
    }

    public boolean isFullyCancelled() {
        return cancelledAmount.compareTo(paidAmount) == 0 && paidAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }
    }
}
