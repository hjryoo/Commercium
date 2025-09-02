package com.commercium.inventory.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
public class StockQuantity {

    private Integer available;      // 사용 가능한 재고
    private Integer reserved;       // 예약된 재고 (주문 대기중)

    protected StockQuantity() {} // JPA용

    private StockQuantity(Integer available, Integer reserved) {
        if (available < 0) {
            throw new IllegalArgumentException("사용 가능한 재고는 0 이상이어야 합니다");
        }
        if (reserved < 0) {
            throw new IllegalArgumentException("예약된 재고는 0 이상이어야 합니다");
        }

        this.available = available;
        this.reserved = reserved;
    }

    public static StockQuantity of(Integer available, Integer reserved) {
        return new StockQuantity(available, reserved);
    }

    public static StockQuantity initialStock(Integer quantity) {
        return new StockQuantity(quantity, 0);
    }

    public Integer getTotalStock() {
        return available + reserved;
    }

    public boolean canReserve(Integer quantity) {
        return available >= quantity;
    }

    public StockQuantity reserve(Integer quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("재고가 부족합니다. 요청: " + quantity + ", 사용가능: " + available);
        }
        return new StockQuantity(available - quantity, reserved + quantity);
    }

    public StockQuantity release(Integer quantity) {
        if (reserved < quantity) {
            throw new IllegalStateException("해제할 예약 재고가 부족합니다. 요청: " + quantity + ", 예약됨: " + reserved);
        }
        return new StockQuantity(available + quantity, reserved - quantity);
    }

    public StockQuantity decrease(Integer quantity) {
        if (reserved < quantity) {
            throw new IllegalStateException("차감할 예약 재고가 부족합니다. 요청: " + quantity + ", 예약됨: " + reserved);
        }
        return new StockQuantity(available, reserved - quantity);
    }

    public StockQuantity increase(Integer quantity) {
        return new StockQuantity(available + quantity, reserved);
    }
}
