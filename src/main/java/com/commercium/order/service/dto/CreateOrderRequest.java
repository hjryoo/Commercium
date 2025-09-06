package com.commercium.order.service.dto;

import com.commercium.order.domain.ShippingAddress;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
    private List<@Valid OrderItemRequest> items;

    @NotNull(message = "배송 주소는 필수입니다")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "상품 ID는 필수입니다")
        private String productId;

        @NotNull(message = "수량은 필수입니다")
        private Integer quantity;
    }

    @Data
    public static class ShippingAddressRequest {
        @NotNull(message = "수령인명은 필수입니다")
        private String recipientName;

        @NotNull(message = "연락처는 필수입니다")
        private String phone;

        @NotNull(message = "우편번호는 필수입니다")
        private String zipCode;

        @NotNull(message = "주소는 필수입니다")
        private String address1;

        private String address2;

        public ShippingAddress toDomain() {
            return ShippingAddress.of(recipientName, phone, zipCode, address1, address2);
        }
    }
}