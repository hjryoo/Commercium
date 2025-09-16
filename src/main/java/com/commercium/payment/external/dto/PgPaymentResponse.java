package com.commercium.payment.external.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PG사 결제 응답 DTO
 * 외부 PG사 API 응답 처리 시 사용
 */
@Data
@Builder
public class PgPaymentResponse {

    private String paymentKey;       // PG사 결제 키
    private String transactionId;    // 거래 ID
    private String orderId;          // 주문 ID
    private BigDecimal amount;       // 결제 금액
    private String status;           // 결제 상태 (PENDING, COMPLETED, FAILED, CANCELLED)
    private String method;           // 실제 사용된 결제 수단
    private LocalDateTime approvedAt; // 승인 시각
    private String receiptUrl;       // 영수증 URL

    // 실패 시 정보
    private String failureCode;      // 실패 코드
    private String failureMessage;   // 실패 메시지

    // 가상계좌 정보 (가상계좌 결제 시)
    private String virtualAccountNumber; // 가상계좌 번호
    private String bankCode;         // 은행 코드
    private LocalDateTime dueDate;   // 입금 마감일

    /**
     * 결제 성공 여부 확인
     */
    public boolean isSuccess() {
        return "COMPLETED".equals(status);
    }

    /**
     * 결제 실패 여부 확인
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 가상계좌 결제 여부 확인
     */
    public boolean isVirtualAccount() {
        return virtualAccountNumber != null && !virtualAccountNumber.isEmpty();
    }
}
