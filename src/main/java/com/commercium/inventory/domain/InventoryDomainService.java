package com.commercium.inventory.domain;

import com.commercium.common.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryDomainService {

    /**
     * 대량 주문 시 재고 검증 로직
     */
    public void validateBulkOrderStock(String productId, Integer quantity) {
        // 대량 주문에 대한 비즈니스 규칙
        if (quantity > 100) {
            throw new BusinessRuleViolationException(
                    String.format("상품 %s는 1회 최대 100개까지만 주문 가능합니다", productId)
            );
        }

        log.info("대량 주문 재고 검증 완료: productId={}, quantity={}", productId, quantity);
    }

    /**
     * 재고 부족 시 알림 임계값 검증
     */
    public boolean shouldNotifyLowStock(StockQuantity stockQuantity, Integer threshold) {
        return stockQuantity.getAvailable() <= threshold;
    }

    /**
     * 재고 조정 시 비즈니스 규칙 검증
     */
    public void validateStockAdjustment(StockQuantity currentStock, Integer newQuantity) {
        if (newQuantity < currentStock.getReserved()) {
            throw new BusinessRuleViolationException(
                    String.format("새 재고 수량(%d)이 예약된 수량(%d)보다 작을 수 없습니다",
                            newQuantity, currentStock.getReserved())
            );
        }
    }
}
