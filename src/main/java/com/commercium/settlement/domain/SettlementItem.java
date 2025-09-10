package com.commercium.settlement.domain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementItem {

    @Id
    @Column(name = "settlement_item_id")
    private String settlementItemId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "order_item_id", nullable = false)
    private String orderItemId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "sale_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal saleAmount;

    @Embedded
    @AttributeOverride(name = "rate", column = @Column(name = "commission_rate"))
    private CommissionRate commissionRate;

    @Column(name = "commission_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "vat_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "net_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private SettlementItem(String orderId, String orderItemId, String productId,
                           BigDecimal saleAmount, CommissionRate commissionRate) {
        this.settlementItemId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.saleAmount = saleAmount;
        this.commissionRate = commissionRate;

        // 정산 금액 계산
        SettlementAmount amount = SettlementAmount.calculate(saleAmount, commissionRate);
        this.commissionAmount = amount.getCommissionAmount();
        this.vatAmount = amount.getVatAmount();
        this.netAmount = amount.getNetAmount();
        this.createdAt = LocalDateTime.now();
    }

    public static SettlementItem create(String orderId, String orderItemId, String productId,
                                        BigDecimal saleAmount, CommissionRate commissionRate) {
        return new SettlementItem(orderId, orderItemId, productId, saleAmount, commissionRate);
    }

    public SettlementAmount getSettlementAmount() {
        return SettlementAmount.calculate(saleAmount, commissionRate);
    }
}
