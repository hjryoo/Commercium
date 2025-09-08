package com.commercium.payment.service;

import com.commercium.payment.domain.Payment;
import com.commercium.payment.external.PgClient;
import com.commercium.payment.external.dto.PgPaymentRequest;
import com.commercium.payment.external.dto.PgPaymentResponse;
import com.commercium.payment.repository.PaymentRepository;
import com.commercium.payment.service.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;

    /**
     * 결제 처리 (PG사 연동)
     */
    @Transactional
    public void processPayment(Payment payment, PaymentRequest request) {
        log.info("PG사 결제 처리 시작: paymentId={}, provider={}",
                payment.getPaymentId().getValue(), payment.getProvider().getName());

        try {
            // PG사 결제 요청 생성
            PgPaymentRequest pgRequest = PgPaymentRequest.builder()
                    .orderName("주문 " + payment.getOrderId())
                    .orderId(payment.getOrderId())
                    .amount(payment.getAmount().getTotalAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .customerName(request.getCustomerName())
                    .customerEmail(request.getCustomerEmail())
                    .returnUrl(request.getReturnUrl())
                    .cancelUrl(request.getCancelUrl())
                    .build();

            // PG사 결제 요청
            PgPaymentResponse pgResponse = pgClient.requestPayment(pgRequest);

            // 결제 처리 상태 업데이트
            payment.startProcessing(pgResponse.getPaymentKey(), pgResponse.getTransactionId());
            paymentRepository.save(payment);

            log.info("PG사 결제 처리 완료: paymentId={}, externalPaymentId={}",
                    payment.getPaymentId().getValue(), pgResponse.getPaymentKey());

        } catch (Exception e) {
            log.error("PG사 결제 처리 실패: paymentId={}", payment.getPaymentId().getValue(), e);
            payment.fail("PG사 통신 오류: " + e.getMessage());
            paymentRepository.save(payment);
            throw e;
        }
    }

    /**
     * 결제 취소 처리 (PG사 연동)
     */
    @Transactional
    public void cancelPayment(Payment payment, BigDecimal cancelAmount, String reason) {
        log.info("PG사 결제 취소 처리 시작: paymentId={}, cancelAmount={}",
                payment.getPaymentId().getValue(), cancelAmount);

        try {
            // PG사 결제 취소 요청
            pgClient.cancelPayment(
                    payment.getProvider().getExternalPaymentId(),
                    cancelAmount,
                    reason
            );

            log.info("PG사 결제 취소 완료: paymentId={}", payment.getPaymentId().getValue());

        } catch (Exception e) {
            log.error("PG사 결제 취소 실패: paymentId={}", payment.getPaymentId().getValue(), e);
            throw e;
        }
    }
}