package com.commercium.order.service.dto;

import com.commercium.order.domain.Order;
import com.commercium.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private String orderId;
    private String orderNumber;
    private OrderStatus status;
    private String statusDescription;
    private BigDecimal totalAmount;
    private ShippingAddressResponse shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId().getValue())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(ShippingAddressResponse.from(order.getShippingAddress()))
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    public static class ShippingAddressResponse {
        private String recipientName;
        private String phone;
        private String zipCode;
        private String address1;
        private String address2;
        private String fullAddress;

        public static ShippingAddressResponse from(com.commercium.order.domain.ShippingAddress address) {
            return ShippingAddressResponse.builder()
                    .recipientName(address.getRecipientName())
                    .phone(address.getPhone())
                    .zipCode(address.getZipCode())
                    .address1(address.getAddress1())
                    .address2(address.getAddress2())
                    .fullAddress(address.getFullAddress())
                    .build();
        }
    }
}

