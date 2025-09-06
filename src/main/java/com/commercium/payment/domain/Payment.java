package com.commercium.payment.domain;

import com.commercium.common.event.DomainEvents;
import com.commercium.payment.event.PaymentCancelledEvent;
import com.commercium.payment.event.PaymentCompletedEvent;
import com.commercium.payment.event.PaymentFailedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @EmbeddedId
    private PaymentId paymentId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "totalAmount", column = @Column(name = "total_amount")),
            @AttributeOverride(name = "paidAmount", column = @Column(name = "paid_amount")),
            @AttributeOverride(name = "cancelledAmount", column = @Column(name = "cancelled_amount"))
    })
    private PaymentAmount amount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "payment_provider")),
            @AttributeOverride(name = "externalPaymentId", column = @Column(name = "external_payment_id")),
            @AttributeOverride(name = "transactionId", column = @Column(name = "transaction_id"))
    })
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "failed_reason")
    private String failedReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Payment(String orderId, PaymentMethod paymentMethod, BigDecimal totalAmount, String providerName) {
        this.paymentId = PaymentId.generate();
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = PaymentAmount.of(totalAmount);
        this.provider = PaymentProvider.of(providerName, null, null);
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Payment create(String orderId, PaymentMethod paymentMethod, BigDecimal totalAmount, String providerName) {
        validatePaymentCreation(orderId, totalAmount);
        return new Payment(orderId, paymentMethod, totalAmount, providerName);
    }

    public void startProcessing(String externalPaymentId, String transactionId) {
        if (!status.isCompletable()) {
            throw new IllegalStateException(
                    String.format("결제 상태가 %s인 경우 처리를 시작할 수 없습니다", status.getDescription())
            );
        }

        this.status = PaymentStatus.PROCESSING;
        this.provider = provider.updateTransactionId(transactionId);
        this.provider = PaymentProvider.of(provider.getName(), externalPaymentId, transactionId);
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(BigDecimal paidAmount) {
        if (!status.isCompletable()) {
            throw new IllegalStateException(
                    String.format("결제 상태가 %s인 경우 완료 처리할 수 없습니다", status.getDescription())
            );
        }

        this.status = PaymentStatus.COMPLETED;
        this.amount = amount.markAsPaid(paidAmount);
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 결제 완료 이벤트 발행
        DomainEvents.raise(new PaymentCompletedEvent(
                paymentId.getValue(),
                orderId,
                paymentMethod,
                paidAmount
        ));
    }

    public void fail(String reason) {
        if (status.isFinal()) {
            throw new IllegalStateException(
                    String.format("결제 상태가 %s인 경우 실패 처리할 수 없습니다", status.getDescription())
            );
        }

        this.status = PaymentStatus.FAILED;
        this.failedReason = reason;
        this.updatedAt = LocalDateTime.now();

        // 결제 실패 이벤트 발행
        DomainEvents.raise(new PaymentFailedEvent(
                paymentId.getValue(),
                orderId,
                reason
        ));
    }

    public void cancel(BigDecimal cancelAmount, String reason) {
        if (!status.isCancellable()) {
            throw new IllegalStateException(
                    String.format("결제 상태가 %s인 경우 취소할 수 없습니다", status.getDescription())
            );
        }

        this.amount = amount.cancel(cancelAmount);

        if (amount.isFullyCancelled()) {
            this.status = PaymentStatus.CANCELLED;
        } else {
            this.status = PaymentStatus.PARTIAL_CANCELLED;
        }

        this.updatedAt = LocalDateTime.now();

        // 결제 취소 이벤트 발행
        DomainEvents.raise(new PaymentCancelledEvent(
                paymentId.getValue(),
                orderId,
                cancelAmount,
                reason
        ));
    }

    public boolean canCancel() {
        return status.isCancellable() && amount.getRefundableAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED && amount.isFullyPaid();
    }

    private static void validatePaymentCreation(String orderId, BigDecimal totalAmount) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("주문 ID는 필수입니다");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다");
        }
    }
}
