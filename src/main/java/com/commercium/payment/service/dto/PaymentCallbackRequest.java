package com.commercium.payment.service.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * PG사 결제 콜백 요청 DTO
 * 외부 PG사에서 결제 완료/실패 시 콜백으로 전송되는 데이터
 */
@Data
public class PaymentCallbackRequest {

    @NotBlank(message = "결제 키는 필수입니다")
    private String paymentKey;          // PG사 결제 키

    @NotBlank(message = "주문 ID는 필수입니다")
    private String orderId;             // 주문 ID

    @NotNull(message = "결제 금액은 필수입니다")
    @DecimalMin(value = "0.0", message = "결제 금액은 0 이상이어야 합니다")
    private BigDecimal amount;          // 결제 금액

    @NotBlank(message = "결제 상태는 필수입니다")
    private String status;              // 결제 상태 (SUCCESS, FAILED, CANCELLED)

    private String method;              // 실제 사용된 결제 수단
    private String transactionId;       // 거래 ID
    private String approvalNumber;      // 승인 번호
    private String receiptUrl;          // 영수증 URL

    // 실패 시 정보
    private String errorCode;           // 실패 코드
    private String errorMessage;        // 실패 메시지

    // 가상계좌 정보 (가상계좌 결제 시)
    private String virtualAccountNumber; // 가상계좌 번호
    private String bankCode;            // 은행 코드
    private String bankName;            // 은행명
    private String depositorName;       // 예금주명
    private String dueDate;             // 입금 마감일 (ISO 8601 형식)

    // 카드 정보 (카드 결제 시)
    private String cardNumber;          // 카드 번호 (마스킹)
    private String cardType;            // 카드 타입
    private String issuerCode;          // 발급사 코드
    private String acquirerCode;        // 매입사 코드

    // 검증용 정보
    private String signature;           // 전자 서명
    private String timestamp;           // 타임스탬프

    /**
     * 결제 성공 여부 확인
     */
    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status);
    }

    /**
     * 결제 실패 여부 확인
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status);
    }

    /**
     * 결제 취소 여부 확인
     */
    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    /**
     * 가상계좌 결제 여부 확인
     */
    public boolean isVirtualAccount() {
        return virtualAccountNumber != null && !virtualAccountNumber.trim().isEmpty();
    }

    /**
     * 에러 정보 포맷팅
     */
    public String getFormattedError() {
        if (errorCode != null && errorMessage != null) {
            return String.format("[%s] %s", errorCode, errorMessage);
        }
        return errorMessage != null ? errorMessage : "알 수 없는 오류";
    }
}
