package com.commercium.inventory.event;

import com.commercium.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class StockDepletedEvent extends DomainEvent {

    private final String productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    public StockDepletedEvent(String productId, Integer requestedQuantity, Integer availableQuantity) {
        super();
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    @Override
    public String getEventType() {
        return "StockDepleted";
    }
}

