package com.commercium.payment.repository;

import com.commercium.payment.domain.Payment;
import com.commercium.payment.domain.PaymentId;
import com.commercium.payment.domain.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(PaymentId paymentId);

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByExternalPaymentId(String externalPaymentId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusAndCreatedAtBetween(PaymentStatus status,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate);

    List<Payment> findByOrderIdIn(List<String> orderIds);

    void delete(Payment payment);
}