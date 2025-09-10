package com.commercium.settlement.repository;

import com.commercium.settlement.domain.Settlement;
import com.commercium.settlement.domain.SettlementId;
import com.commercium.settlement.domain.SettlementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    Optional<Settlement> findById(SettlementId settlementId);

    List<Settlement> findBySellerId(String sellerId);

    List<Settlement> findBySellerIdAndStatus(String sellerId, SettlementStatus status);

    Optional<Settlement> findBySellerIdAndPeriod(String sellerId, LocalDate startDate, LocalDate endDate);

    List<Settlement> findByStatus(SettlementStatus status);

    List<Settlement> findBySettlementDateBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Settlement> findBySellerIdAndSettlementDateBetween(String sellerId,
                                                            LocalDateTime startDateTime,
                                                            LocalDateTime endDateTime);

    boolean existsBySellerIdAndPeriod(String sellerId, LocalDate startDate, LocalDate endDate);

    void delete(Settlement settlement);
}
