package com.commercium.inventory.event;

import com.commercium.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class StockReservedEvent extends DomainEvent {

    private final String productId;
    private final String orderId;
    private final Integer quantity;

    public StockReservedEvent(String productId, String orderId, Integer quantity) {
        super();
        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
    }

    @Override
    public String getEventType() {
        return "StockReserved";
    }
}
