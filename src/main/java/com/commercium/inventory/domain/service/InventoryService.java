package com.commercium.inventory.domain.service;

import com.commercium.common.exception.BusinessRuleViolationException;
import com.commercium.inventory.domain.Inventory;
import com.commercium.inventory.domain.InventoryTransaction;
import com.commercium.inventory.domain.ProductId;
import com.commercium.inventory.repository.InventoryRepository;
import com.commercium.inventory.repository.InventoryTransactionRepository;
import com.commercium.inventory.service.dto.InventoryResponse;
import com.commercium.inventory.service.dto.StockTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;

    /**
     * 상품 재고 생성
     */
    public InventoryResponse createInventory(String productId, Integer initialQuantity) {
        ProductId prodId = ProductId.of(productId);

        if (inventoryRepository.existsByProductId(prodId)) {
            throw new BusinessRuleViolationException("이미 재고가 존재하는 상품입니다");
        }

        Inventory inventory = Inventory.create(prodId, initialQuantity);
        Inventory savedInventory = inventoryRepository.save(inventory);

        log.info("재고 생성 완료: productId={}, initialQuantity={}", productId, initialQuantity);

        return InventoryResponse.from(savedInventory);
    }

    /**
     * 재고 조회
     */
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(String productId) {
        ProductId prodId = ProductId.of(productId);
        Inventory inventory = inventoryRepository.findByProductId(prodId)
                .orElseThrow(() -> new BusinessRuleViolationException("재고 정보를 찾을 수 없습니다"));

        return InventoryResponse.from(inventory);
    }

    /**
     * 재고 입고
     */
    public void increaseStock(String productId, Integer quantity, String reason) {
        ProductId prodId = ProductId.of(productId);
        Inventory inventory = inventoryRepository.findByProductId(prodId)
                .orElseThrow(() -> new BusinessRuleViolationException("재고 정보를 찾을 수 없습니다"));

        inventory.increase(quantity, reason);
        inventoryRepository.save(inventory);

        log.info("재고 입고 완료: productId={}, quantity={}", productId, quantity);
    }

    /**
     * 관리자 재고 조정
     */
    public void adjustStock(String productId, Integer newQuantity, String reason) {
        ProductId prodId = ProductId.of(productId);
        Inventory inventory = inventoryRepository.findByProductId(prodId)
                .orElseThrow(() -> new BusinessRuleViolationException("재고 정보를 찾을 수 없습니다"));

        inventory.adjust(newQuantity, reason);
        inventoryRepository.save(inventory);

        log.info("재고 조정 완료: productId={}, newQuantity={}", productId, newQuantity);
    }

    /**
     * 재고 트랜잭션 이력 조회
     */
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getStockTransactions(String productId) {
        ProductId prodId = ProductId.of(productId);
        List<InventoryTransaction> transactions = transactionRepository.findByProductId(prodId);

        return transactions.stream()
                .map(StockTransactionResponse::from)
                .toList();
    }

    /**
     * 주문별 재고 트랜잭션 조회
     */
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getOrderStockTransactions(String orderId) {
        List<InventoryTransaction> transactions = transactionRepository.findByOrderId(orderId);

        return transactions.stream()
                .map(StockTransactionResponse::from)
                .toList();
    }
}

