package com.commercium.payment.service;
import com.commercium.common.exception.BusinessRuleViolationException;
import com.commercium.payment.domain.Payment;
import com.commercium.payment.domain.PaymentId;
import com.commercium.payment.repository.PaymentRepository;
import com.commercium.payment.service.dto.PaymentRequest;
import com.commercium.payment.service.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProcessingService paymentProcessingService;

    /**
     * 결제 생성 및 처리 요청
     */
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("결제 생성 요청: orderId={}, method={}, amount={}",
                request.getOrderId(), request.getPaymentMethod(), request.getAmount());

        // 중복 결제 확인
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BusinessRuleViolationException("이미 결제가 진행 중인 주문입니다");
        }

        // 결제 객체 생성
        Payment payment = Payment.create(
                request.getOrderId(),
                request.getPaymentMethod(),
                request.getAmount(),
                "MOCK_PG" // 실제로는 설정에서 가져옴
        );

        Payment savedPayment = paymentRepository.save(payment);

        // 결제 처리 서비스에 위임
        paymentProcessingService.processPayment(savedPayment, request);

        log.info("결제 생성 완료: paymentId={}", savedPayment.getPaymentId().getValue());

        return PaymentResponse.from(savedPayment);
    }

    /**
     * 결제 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new BusinessRuleViolationException("결제 정보를 찾을 수 없습니다"));

        return PaymentResponse.from(payment);
    }

    /**
     * 주문별 결제 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessRuleViolationException("결제 정보를 찾을 수 없습니다"));

        return PaymentResponse.from(payment);
    }

    /**
     * 결제 취소
     */
    public PaymentResponse cancelPayment(String paymentId, BigDecimal cancelAmount, String reason) {
        log.info("결제 취소 요청: paymentId={}, cancelAmount={}, reason={}",
                paymentId, cancelAmount, reason);

        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new BusinessRuleViolationException("결제 정보를 찾을 수 없습니다"));

        if (!payment.canCancel()) {
            throw new BusinessRuleViolationException("취소할 수 없는 결제입니다");
        }

        // 외부 PG사에 취소 요청
        paymentProcessingService.cancelPayment(payment, cancelAmount, reason);

        payment.cancel(cancelAmount, reason);
        Payment savedPayment = paymentRepository.save(payment);

        log.info("결제 취소 완료: paymentId={}", paymentId);

        return PaymentResponse.from(savedPayment);
    }

    /**
     * 결제 완료 처리 (PG사 콜백에서 호출)
     */
    public void completePayment(String externalPaymentId, BigDecimal paidAmount) {
        log.info("결제 완료 처리: externalPaymentId={}, paidAmount={}", externalPaymentId, paidAmount);

        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
                .orElseThrow(() -> new BusinessRuleViolationException("결제 정보를 찾을 수 없습니다"));

        payment.complete(paidAmount);
        paymentRepository.save(payment);

        log.info("결제 완료 처리 완료: paymentId={}", payment.getPaymentId().getValue());
    }

    /**
     * 결제 실패 처리 (PG사 콜백에서 호출)
     */
    public void failPayment(String externalPaymentId, String reason) {
        log.info("결제 실패 처리: externalPaymentId={}, reason={}", externalPaymentId, reason);

        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
                .orElseThrow(() -> new BusinessRuleViolationException("결제 정보를 찾을 수 없습니다"));

        payment.fail(reason);
        paymentRepository.save(payment);

        log.info("결제 실패 처리 완료: paymentId={}", payment.getPaymentId().getValue());
    }

    /**
     * 다중 주문 결제 조회 (정산용)
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrders(List<String> orderIds) {
        return paymentRepository.findByOrderIdIn(orderIds)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }
}