package com.commercium.payment.external;

import com.commercium.payment.domain.PaymentMethod;
import com.commercium.payment.external.dto.PgPaymentRequest;
import com.commercium.payment.external.dto.PgPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock PG 클라이언트 구현체
 * 개발/테스트 환경에서 실제 PG사 없이 결제 테스트 가능
 */
@Component
@Slf4j
public class MockPgClient implements PgClient {

    @Override
    public PgPaymentResponse requestPayment(PgPaymentRequest request) {
        log.info("Mock PG 결제 요청: orderId={}, amount={}, method={}",
                request.getOrderId(), request.getAmount(), request.getPaymentMethod());

        // Mock 응답 생성
        String paymentKey = "mock_payment_" + UUID.randomUUID().toString();
        String transactionId = "mock_tx_" + System.currentTimeMillis();

        // 결제 방법별 처리 시뮬레이션
        boolean success = simulatePaymentSuccess(request.getPaymentMethod());

        PgPaymentResponse response = PgPaymentResponse.builder()
                .paymentKey(paymentKey)
                .transactionId(transactionId)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(success ? "COMPLETED" : "FAILED")
                .approvedAt(success ? LocalDateTime.now() : null)
                .failureCode(success ? null : "INSUFFICIENT_BALANCE")
                .failureMessage(success ? null : "잔액 부족")
                .build();

        log.info("Mock PG 결제 응답: paymentKey={}, status={}", paymentKey, response.getStatus());

        return response;
    }

    @Override
    public void cancelPayment(String paymentKey, BigDecimal cancelAmount, String reason) {
        log.info("Mock PG 결제 취소: paymentKey={}, amount={}, reason={}",
                paymentKey, cancelAmount, reason);

        // Mock 취소 처리 시뮬레이션
        // 실제로는 외부 PG사 API 호출
    }

    @Override
    public PgPaymentResponse getPayment(String paymentKey) {
        log.info("Mock PG 결제 조회: paymentKey={}", paymentKey);

        // Mock 조회 응답
        return PgPaymentResponse.builder()
                .paymentKey(paymentKey)
                .status("COMPLETED")
                .amount(BigDecimal.valueOf(10000))
                .approvedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 결제 방법별 성공률 시뮬레이션
     */
    private boolean simulatePaymentSuccess(PaymentMethod method) {
        return switch (method) {
            case CARD -> Math.random() > 0.1; // 90% 성공률
            case BANK_TRANSFER -> Math.random() > 0.05; // 95% 성공률
            case VIRTUAL_ACCOUNT -> true; // 가상계좌는 항상 성공 (입금 대기)
            default -> Math.random() > 0.2; // 80% 성공률
        };
    }
}