package com.commercium.inventory.domain.service;

import com.commercium.common.exception.BusinessRuleViolationException;
import com.commercium.inventory.domain.Inventory;
import com.commercium.inventory.domain.ProductId;
import com.commercium.inventory.infrastructure.RedisStockLockManager;
import com.commercium.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockReservationService {

    private final InventoryRepository inventoryRepository;
    private final RedisStockLockManager lockManager;

    /**
     * 재고 예약 (분산락 사용)
     */
    @Transactional
    public void reserveStock(String productId, String orderId, Integer quantity) {
        String lockKey = "stock:reserve:" + productId;

        boolean acquired = lockManager.tryLock(lockKey, Duration.ofSeconds(10));
        if (!acquired) {
            throw new BusinessRuleViolationException("재고 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            log.info("재고 예약 시작: productId={}, orderId={}, quantity={}", productId, orderId, quantity);

            ProductId prodId = ProductId.of(productId);
            Inventory inventory = inventoryRepository.findByProductId(prodId)
                    .orElseThrow(() -> new BusinessRuleViolationException("상품 재고 정보를 찾을 수 없습니다"));

            inventory.reserve(orderId, quantity, "주문 생성으로 인한 재고 예약");
            inventoryRepository.save(inventory);

            log.info("재고 예약 완료: productId={}, 사용가능재고={}, 예약재고={}",
                    productId, inventory.getStockQuantity().getAvailable(), inventory.getStockQuantity().getReserved());

        } finally {
            lockManager.unlock(lockKey);
        }
    }

    /**
     * 예약 해제 (주문 취소 시)
     */
    @Transactional
    public void releaseReservation(String productId, String orderId, Integer quantity) {
        String lockKey = "stock:release:" + productId;

        boolean acquired = lockManager.tryLock(lockKey, Duration.ofSeconds(10));
        if (!acquired) {
            throw new BusinessRuleViolationException("재고 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            log.info("재고 예약 해제 시작: productId={}, orderId={}, quantity={}", productId, orderId, quantity);

            ProductId prodId = ProductId.of(productId);
            Inventory inventory = inventoryRepository.findByProductId(prodId)
                    .orElseThrow(() -> new BusinessRuleViolationException("상품 재고 정보를 찾을 수 없습니다"));

            inventory.releaseReservation(orderId, quantity, "주문 취소로 인한 재고 해제");
            inventoryRepository.save(inventory);

            log.info("재고 예약 해제 완료: productId={}", productId);

        } finally {
            lockManager.unlock(lockKey);
        }
    }

    /**
     * 재고 차감 (결제 완료 시)
     */
    @Transactional
    public void decreaseStock(String productId, String orderId, Integer quantity) {
        String lockKey = "stock:decrease:" + productId;

        boolean acquired = lockManager.tryLock(lockKey, Duration.ofSeconds(10));
        if (!acquired) {
            throw new BusinessRuleViolationException("재고 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            log.info("재고 차감 시작: productId={}, orderId={}, quantity={}", productId, orderId, quantity);

            ProductId prodId = ProductId.of(productId);
            Inventory inventory = inventoryRepository.findByProductId(prodId)
                    .orElseThrow(() -> new BusinessRuleViolationException("상품 재고 정보를 찾을 수 없습니다"));

            inventory.decrease(orderId, quantity, "결제 완료로 인한 재고 차감");
            inventoryRepository.save(inventory);

            log.info("재고 차감 완료: productId={}", productId);

        } finally {
            lockManager.unlock(lockKey);
        }
    }
}