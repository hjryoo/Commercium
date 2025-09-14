package com.commercium.settlement.event;
import com.commercium.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SettlementCompletedEvent extends DomainEvent {

    private final String settlementId;
    private final String sellerId;
    private final BigDecimal netAmount;
    private final Integer itemCount;

    public SettlementCompletedEvent(String settlementId, String sellerId,
                                    BigDecimal netAmount, Integer itemCount) {
        super();
        this.settlementId = settlementId;
        this.sellerId = sellerId;
        this.netAmount = netAmount;
        this.itemCount = itemCount;
    }

    @Override
    public String getEventType() {
        return "SettlementCompleted";
    }
}