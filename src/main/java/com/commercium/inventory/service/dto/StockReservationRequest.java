package com.commercium.inventory.service.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class StockReservationRequest {

    @NotNull(message = "상품 ID는 필수입니다")
    private String productId;

    @NotNull(message = "주문 ID는 필수입니다")
    private String orderId;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;
}

