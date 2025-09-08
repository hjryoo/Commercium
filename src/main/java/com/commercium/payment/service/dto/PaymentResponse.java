package com.commercium.payment.service.dto;
import com.commercium.payment.domain.Payment;
import com.commercium.payment.domain.PaymentMethod;
import com.commercium.payment.domain.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private String paymentId;
    private String orderId;
    private PaymentMethod paymentMethod;
    private String paymentMethodDescription;
    private PaymentStatus status;
    private String statusDescription;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal cancelledAmount;
    private BigDecimal refundableAmount;
    private String paymentProvider;
    private String externalPaymentId;
    private String transactionId;
    private String failedReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId().getValue())
                .orderId(payment.getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentMethodDescription(payment.getPaymentMethod().getDescription())
                .status(payment.getStatus())
                .statusDescription(payment.getStatus().getDescription())
                .totalAmount(payment.getAmount().getTotalAmount())
                .paidAmount(payment.getAmount().getPaidAmount())
                .cancelledAmount(payment.getAmount().getCancelledAmount())
                .refundableAmount(payment.getAmount().getRefundableAmount())
                .paymentProvider(payment.getProvider().getName())
                .externalPaymentId(payment.getProvider().getExternalPaymentId())
                .transactionId(payment.getProvider().getTransactionId())
                .failedReason(payment.getFailedReason())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
