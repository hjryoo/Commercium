package com.commercium.order.event;

import com.commercium.common.event.DomainEvent;
import com.commercium.order.domain.OrderItem;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCancelledEvent extends DomainEvent {

    private final String orderId;
    private final List<OrderItem> orderItems;

    public OrderCancelledEvent(String orderId, List<OrderItem> orderItems) {
        super();
        this.orderId = orderId;
        this.orderItems = List.copyOf(orderItems); // 불변 복사
    }

    @Override
    public String getEventType() {
        return "OrderCancelled";
    }
}