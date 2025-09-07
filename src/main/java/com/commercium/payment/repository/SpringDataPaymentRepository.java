package com.commercium.payment.repository;
import com.commercium.payment.domain.Payment;
import com.commercium.payment.domain.PaymentId;
import com.commercium.payment.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface SpringDataPaymentRepository extends JpaRepository<Payment, PaymentId> {

    Optional<Payment> findByOrderId(String orderId);

    @Query("SELECT p FROM Payment p WHERE p.provider.externalPaymentId = :externalPaymentId")
    Optional<Payment> findByExternalPaymentId(@Param("externalPaymentId") String externalPaymentId);

    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    List<Payment> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(PaymentStatus status,
                                                                      LocalDateTime startDate,
                                                                      LocalDateTime endDate);

    List<Payment> findByOrderIdInOrderByCreatedAtDesc(List<String> orderIds);
}

@Repository
@RequiredArgsConstructor
public class JpaPaymentRepository implements PaymentRepository {

    private final SpringDataPaymentRepository repository;

    @Override
    public Payment save(Payment payment) {
        return repository.save(payment);
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return repository.findById(paymentId);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }

    @Override
    public Optional<Payment> findByExternalPaymentId(String externalPaymentId) {
        return repository.findByExternalPaymentId(externalPaymentId);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return repository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<Payment> findByStatusAndCreatedAtBetween(PaymentStatus status,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate) {
        return repository.findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(status, startDate, endDate);
    }

    @Override
    public List<Payment> findByOrderIdIn(List<String> orderIds) {
        return repository.findByOrderIdInOrderByCreatedAtDesc(orderIds);
    }

    @Override
    public void delete(Payment payment) {
        repository.delete(payment);
    }
}
