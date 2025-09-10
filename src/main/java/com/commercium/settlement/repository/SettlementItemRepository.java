package com.commercium.settlement.repository;

import com.commercium.settlement.domain.SettlementItem;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementItemRepository {

    SettlementItem save(SettlementItem settlementItem);

    List<SettlementItem> saveAll(List<SettlementItem> settlementItems);

    List<SettlementItem> findByOrderId(String orderId);

    List<SettlementItem> findByProductId(String productId);

    List<SettlementItem> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}