package com.commercium.payment.external;

import com.commercium.payment.external.dto.PgPaymentRequest;
import com.commercium.payment.external.dto.PgPaymentResponse;

import java.math.BigDecimal;

/**
 * PG(Payment Gateway) 연동 클라이언트 인터페이스
 * 다양한 PG사(토스페이먼츠, KCP, INICIS 등)를 지원하기 위한 추상화
 */
public interface PgClient {

    /**
     * 결제 요청
     * @param request 결제 요청 정보
     * @return 결제 응답 정보
     */
    PgPaymentResponse requestPayment(PgPaymentRequest request);

    /**
     * 결제 취소
     * @param paymentKey PG사 결제 키
     * @param cancelAmount 취소 금액
     * @param reason 취소 사유
     */
    void cancelPayment(String paymentKey, BigDecimal cancelAmount, String reason);

    /**
     * 결제 조회
     * @param paymentKey PG사 결제 키
     * @return 결제 정보
     */
    PgPaymentResponse getPayment(String paymentKey);
}