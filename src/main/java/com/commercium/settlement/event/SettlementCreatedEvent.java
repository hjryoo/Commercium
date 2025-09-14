package com.commercium.settlement.event;

import com.commercium.common.event.DomainEvent;
import com.commercium.settlement.domain.SettlementPeriod;
import lombok.Getter;

@Getter
public class SettlementCreatedEvent extends DomainEvent {

    private final String settlementId;
    private final String sellerId;
    private final SettlementPeriod period;

    public SettlementCreatedEvent(String settlementId, String sellerId, SettlementPeriod period) {
        super();
        this.settlementId = settlementId;
        this.sellerId = sellerId;
        this.period = period;
    }

    @Override
    public String getEventType() {
        return "SettlementCreated";
    }
}
