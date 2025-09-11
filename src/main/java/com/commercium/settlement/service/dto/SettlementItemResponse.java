package com.commercium.settlement.service.dto;
import com.commercium.settlement.domain.SettlementItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SettlementItemResponse {

    private String settlementItemId;
    private String orderId;
    private String orderItemId;
    private String productId;
    private BigDecimal saleAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal vatAmount;
    private BigDecimal netAmount;
    private LocalDateTime createdAt;

    public static SettlementItemResponse from(SettlementItem settlementItem) {
        return SettlementItemResponse.builder()
                .settlementItemId(settlementItem.getSettlementItemId())
                .orderId(settlementItem.getOrderId())
                .orderItemId(settlementItem.getOrderItemId())
                .productId(settlementItem.getProductId())
                .saleAmount(settlementItem.getSaleAmount())
                .commissionRate(settlementItem.getCommissionRate().getPercentage())
                .commissionAmount(settlementItem.getCommissionAmount())
                .vatAmount(settlementItem.getVatAmount())
                .netAmount(settlementItem.getNetAmount())
                .createdAt(settlementItem.getCreatedAt())
                .build();
    }
}