package com.commercium.payment.service.dto;
import com.commercium.payment.domain.PaymentMethod;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotNull(message = "주문 ID는 필수입니다")
    private String orderId;

    @NotNull(message = "결제 방법은 필수입니다")
    private PaymentMethod paymentMethod;

    @NotNull(message = "결제 금액은 필수입니다")
    @DecimalMin(value = "1.0", message = "결제 금액은 1원 이상이어야 합니다")
    private BigDecimal amount;

    private String returnUrl;      // 결제 완료 후 리다이렉트 URL
    private String cancelUrl;      // 결제 취소 후 리다이렉트 URL
    private String customerName;   // 고객명
    private String customerEmail;  // 고객 이메일
}