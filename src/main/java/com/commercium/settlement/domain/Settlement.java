package com.commercium.settlement.domain;

import com.commercium.common.event.DomainEvents;
import com.commercium.settlement.event.SettlementCompletedEvent;
import com.commercium.settlement.event.SettlementCreatedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @EmbeddedId
    private SettlementId settlementId;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "period_start")),
            @AttributeOverride(name = "endDate", column = @Column(name = "period_end"))
    })
    private SettlementPeriod period;

    @Column(name = "settlement_date", nullable = false)
    private LocalDateTime settlementDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "totalSales", column = @Column(name = "total_sales")),
            @AttributeOverride(name = "commissionAmount", column = @Column(name = "commission_amount")),
            @AttributeOverride(name = "vatAmount", column = @Column(name = "vat_amount")),
            @AttributeOverride(name = "netAmount", column = @Column(name = "net_amount"))
    })
    private SettlementAmount amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "settlement_id")
    private List<SettlementItem> settlementItems = new ArrayList<>();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Settlement(String sellerId, SettlementPeriod period) {
        this.settlementId = SettlementId.generate();
        this.sellerId = sellerId;
        this.period = period;
        this.settlementDate = LocalDateTime.now();
        this.amount = SettlementAmount.zero();
        this.status = SettlementStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 정산 생성 이벤트 발행
        DomainEvents.raise(new SettlementCreatedEvent(
                settlementId.getValue(),
                sellerId,
                period
        ));
    }

    public static Settlement create(String sellerId, SettlementPeriod period) {
        validateSettlementCreation(sellerId, period);
        return new Settlement(sellerId, period);
    }

    public void addSettlementItem(SettlementItem item) {
        if (status.isFinal()) {
            throw new IllegalStateException(
                    String.format("정산 상태가 %s인 경우 항목을 추가할 수 없습니다", status.getDescription())
            );
        }

        settlementItems.add(item);
        recalculateAmount();
        this.updatedAt = LocalDateTime.now();
    }

    public void addSettlementItems(List<SettlementItem> items) {
        items.forEach(this::addSettlementItem);
    }

    public void startCalculation() {
        if (!status.isCompletable()) {
            throw new IllegalStateException(
                    String.format("정산 상태가 %s인 경우 계산을 시작할 수 없습니다", status.getDescription())
            );
        }

        this.status = SettlementStatus.CALCULATING;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        if (!status.isCompletable()) {
            throw new IllegalStateException(
                    String.format("정산 상태가 %s인 경우 완료할 수 없습니다", status.getDescription())
            );
        }

        this.status = SettlementStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 정산 완료 이벤트 발행
        DomainEvents.raise(new SettlementCompletedEvent(
                settlementId.getValue(),
                sellerId,
                amount.getNetAmount(),
                settlementItems.size()
        ));
    }

    public void fail(String reason) {
        this.status = SettlementStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (status == SettlementStatus.COMPLETED) {
            throw new IllegalStateException("완료된 정산은 취소할 수 없습니다");
        }

        this.status = SettlementStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public List<SettlementItem> getSettlementItems() {
        return Collections.unmodifiableList(settlementItems);
    }

    public boolean hasItems() {
        return !settlementItems.isEmpty();
    }

    public int getItemCount() {
        return settlementItems.size();
    }

    private void recalculateAmount() {
        this.amount = settlementItems.stream()
                .map(SettlementItem::getSettlementAmount)
                .reduce(SettlementAmount.zero(), SettlementAmount::add);
    }

    private static void validateSettlementCreation(String sellerId, SettlementPeriod period) {
        if (sellerId == null || sellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("판매자 ID는 필수입니다");
        }
        if (period == null) {
            throw new IllegalArgumentException("정산 기간은 필수입니다");
        }
    }
}
