package com.commercium.payment.external.dto;

import com.commercium.payment.domain.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * PG사 결제 요청 DTO
 * 외부 PG사 API 호출 시 사용
 */
@Data
@Builder
public class PgPaymentRequest {

    private String orderName;        // 주문명
    private String orderId;          // 주문 ID
    private BigDecimal amount;       // 결제 금액
    private PaymentMethod paymentMethod; // 결제 수단
    private String customerName;     // 고객명
    private String customerEmail;    // 고객 이메일
    private String customerPhone;    // 고객 전화번호
    private String returnUrl;        // 결제 완료 후 리다이렉트 URL
    private String cancelUrl;        // 결제 취소 후 리다이렉트 URL
    private String callbackUrl;      // 결제 결과 콜백 URL

    /**
     * 주문 정보로부터 PG 요청 생성
     */
    public static PgPaymentRequest from(String orderId, BigDecimal amount,
                                        PaymentMethod paymentMethod, String customerInfo) {
        return PgPaymentRequest.builder()
                .orderName("주문 " + orderId)
                .orderId(orderId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .customerName(customerInfo)
                .returnUrl("https://example.com/payment/success")
                .cancelUrl("https://example.com/payment/cancel")
                .callbackUrl("https://example.com/api/payments/callback")
                .build();
    }
}