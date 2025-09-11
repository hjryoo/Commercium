package com.commercium.settlement.service;
import com.commercium.settlement.domain.Settlement;
import com.commercium.settlement.domain.SettlementStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SettlementResponse {

    private String settlementId;
    private String sellerId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String periodDescription;
    private SettlementStatus status;
    private String statusDescription;
    private BigDecimal totalSales;
    private BigDecimal commissionAmount;
    private BigDecimal vatAmount;
    private BigDecimal netAmount;
    private BigDecimal commissionRate;
    private int itemCount;
    private List<SettlementItemResponse> items;
    private LocalDateTime settlementDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SettlementResponse from(Settlement settlement) {
        return SettlementResponse.builder()
                .settlementId(settlement.getSettlementId().getValue())
                .sellerId(settlement.getSellerId())
                .periodStart(settlement.getPeriod().getStartDate())
                .periodEnd(settlement.getPeriod().getEndDate())
                .periodDescription(settlement.getPeriod().getDescription())
                .status(settlement.getStatus())
                .statusDescription(settlement.getStatus().getDescription())
                .totalSales(settlement.getAmount().getTotalSales())
                .commissionAmount(settlement.getAmount().getCommissionAmount())
                .vatAmount(settlement.getAmount().getVatAmount())
                .netAmount(settlement.getAmount().getNetAmount())
                .commissionRate(settlement.getAmount().getCommissionRate())
                .itemCount(settlement.getItemCount())
                .items(settlement.getSettlementItems().stream()
                        .map(SettlementItemResponse::from)
                        .toList())
                .settlementDate(settlement.getSettlementDate())
                .completedAt(settlement.getCompletedAt())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }

    public static SettlementResponse fromWithoutItems(Settlement settlement) {
        return from(settlement).toBuilder()
                .items(null)
                .build();
    }
}