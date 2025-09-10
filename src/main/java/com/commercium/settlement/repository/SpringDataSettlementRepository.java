package com.commercium.settlement.repository;

import com.commercium.settlement.domain.Settlement;
import com.commercium.settlement.domain.SettlementId;
import com.commercium.settlement.domain.SettlementItem;
import com.commercium.settlement.domain.SettlementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface SpringDataSettlementRepository extends JpaRepository<Settlement, SettlementId> {

    List<Settlement> findBySellerIdOrderByCreatedAtDesc(String sellerId);

    List<Settlement> findBySellerIdAndStatusOrderByCreatedAtDesc(String sellerId, SettlementStatus status);

    @Query("SELECT s FROM Settlement s WHERE s.sellerId = :sellerId AND s.period.startDate = :startDate AND s.period.endDate = :endDate")
    Optional<Settlement> findBySellerIdAndPeriod(@Param("sellerId") String sellerId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    List<Settlement> findByStatusOrderByCreatedAtDesc(SettlementStatus status);

    List<Settlement> findBySettlementDateBetweenOrderBySettlementDateDesc(LocalDateTime startDateTime,
                                                                          LocalDateTime endDateTime);

    List<Settlement> findBySellerIdAndSettlementDateBetweenOrderBySettlementDateDesc(String sellerId,
                                                                                     LocalDateTime startDateTime,
                                                                                     LocalDateTime endDateTime);

    @Query("SELECT COUNT(s) > 0 FROM Settlement s WHERE s.sellerId = :sellerId AND s.period.startDate = :startDate AND s.period.endDate = :endDate")
    boolean existsBySellerIdAndPeriod(@Param("sellerId") String sellerId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
}

interface SpringDataSettlementItemRepository extends JpaRepository<SettlementItem, String> {

    List<SettlementItem> findByOrderIdOrderByCreatedAtDesc(String orderId);

    List<SettlementItem> findByProductIdOrderByCreatedAtDesc(String productId);

    List<SettlementItem> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDateTime,
                                                                    LocalDateTime endDateTime);
}

@Repository
@RequiredArgsConstructor
public class JpaSettlementRepository implements SettlementRepository, SettlementItemRepository {

    private final SpringDataSettlementRepository settlementRepository;
    private final SpringDataSettlementItemRepository settlementItemRepository;

    @Override
    public Settlement save(Settlement settlement) {
        return settlementRepository.save(settlement);
    }

    @Override
    public Optional<Settlement> findById(SettlementId settlementId) {
        return settlementRepository.findById(settlementId);
    }

    @Override
    public List<Settlement> findBySellerId(String sellerId) {
        return settlementRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    @Override
    public List<Settlement> findBySellerIdAndStatus(String sellerId, SettlementStatus status) {
        return settlementRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, status);
    }

    @Override
    public Optional<Settlement> findBySellerIdAndPeriod(String sellerId, LocalDate startDate, LocalDate endDate) {
        return settlementRepository.findBySellerIdAndPeriod(sellerId, startDate, endDate);
    }

    @Override
    public List<Settlement> findByStatus(SettlementStatus status) {
        return settlementRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<Settlement> findBySettlementDateBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return settlementRepository.findBySettlementDateBetweenOrderBySettlementDateDesc(startDateTime, endDateTime);
    }

    @Override
    public List<Settlement> findBySellerIdAndSettlementDateBetween(String sellerId,
                                                                   LocalDateTime startDateTime,
                                                                   LocalDateTime endDateTime) {
        return settlementRepository.findBySellerIdAndSettlementDateBetweenOrderBySettlementDateDesc(
                sellerId, startDateTime, endDateTime);
    }

    @Override
    public boolean existsBySellerIdAndPeriod(String sellerId, LocalDate startDate, LocalDate endDate) {
        return settlementRepository.existsBySellerIdAndPeriod(sellerId, startDate, endDate);
    }

    @Override
    public void delete(Settlement settlement) {
        settlementRepository.delete(settlement);
    }

    // SettlementItem 메서드들
    @Override
    public SettlementItem save(SettlementItem settlementItem) {
        return settlementItemRepository.save(settlementItem);
    }

    @Override
    public List<SettlementItem> saveAll(List<SettlementItem> settlementItems) {
        return settlementItemRepository.saveAll(settlementItems);
    }

    @Override
    public List<SettlementItem> findByOrderId(String orderId) {
        return settlementItemRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    public List<SettlementItem> findByProductId(String productId) {
        return settlementItemRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public List<SettlementItem> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return settlementItemRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDateTime, endDateTime);
    }
}