package com.commercium.inventory.event;

import com.commercium.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class StockReleasedEvent extends DomainEvent {

    private final String productId;
    private final String orderId;
    private final Integer quantity;

    public StockReleasedEvent(String productId, String orderId, Integer quantity) {
        super();
        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
    }

    @Override
    public String getEventType() {
        return "StockReleased";
    }
}
