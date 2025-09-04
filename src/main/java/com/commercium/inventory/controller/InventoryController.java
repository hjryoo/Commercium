package com.commercium.inventory.controller;

import com.commercium.common.dto.ApiResponse;
import com.commercium.inventory.service.InventoryService;
import com.commercium.inventory.service.StockReservationService;
import com.commercium.inventory.service.dto.InventoryResponse;
import com.commercium.inventory.service.dto.StockReservationRequest;
import com.commercium.inventory.service.dto.StockTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "재고 관리", description = "상품 재고 조회, 관리, 예약 등의 기능을 제공합니다")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;
    private final StockReservationService stockReservationService;

    @PostMapping("/{productId}")
    @Operation(summary = "재고 생성", description = "새로운 상품의 재고를 생성합니다")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable String productId,
            @Parameter(description = "초기 재고 수량", required = true)
            @RequestParam @NotNull @Min(0) Integer initialQuantity) {

        log.info("재고 생성 요청: productId={}, initialQuantity={}", productId, initialQuantity);

        InventoryResponse response = inventoryService.createInventory(productId, initialQuantity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "재고가 성공적으로 생성되었습니다"));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "재고 조회", description = "특정 상품의 재고 정보를 조회합니다")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable String productId) {

        log.info("재고 조회 요청: productId={}", productId);

        InventoryResponse response = inventoryService.getInventory(productId);

        return ResponseEntity.ok(ApiResponse.success(response, "재고 정보를 성공적으로 조회했습니다"));
    }

    @PatchMapping("/{productId}/increase")
    @Operation(summary = "재고 입고", description = "상품 재고를 증가시킵니다")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> increaseStock(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable String productId,
            @Parameter(description = "입고 수량", required = true)
            @RequestParam @NotNull @Min(1) Integer quantity,
            @Parameter(description = "입고 사유")
            @RequestParam(required = false, defaultValue = "관리자 입고") String reason) {

        log.info("재고 입고 요청: productId={}, quantity={}, reason={}", productId, quantity, reason);

        inventoryService.increaseStock(productId, quantity, reason);

        return ResponseEntity.ok(ApiResponse.success(null, "재고 입고가 완료되었습니다"));
    }

    @PutMapping("/{productId}/adjust")
    @Operation(summary = "재고 조정", description = "상품 재고를 특정 수량으로 조정합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adjustStock(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable String productId,
            @Parameter(description = "조정할 재고 수량", required = true)
            @RequestParam @NotNull @Min(0) Integer newQuantity,
            @Parameter(description = "조정 사유")
            @RequestParam(required = false, defaultValue = "관리자 재고 조정") String reason) {

        log.info("재고 조정 요청: productId={}, newQuantity={}, reason={}", productId, newQuantity, reason);

        inventoryService.adjustStock(productId, newQuantity, reason);

        return ResponseEntity.ok(ApiResponse.success(null, "재고 조정이 완료되었습니다"));
    }

    @GetMapping("/{productId}/transactions")
    @Operation(summary = "재고 이력 조회", description = "특정 상품의 재고 변동 이력을 조회합니다")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getStockTransactions(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable String productId) {

        log.info("재고 이력 조회 요청: productId={}", productId);

        List<StockTransactionResponse> transactions = inventoryService.getStockTransactions(productId);

        return ResponseEntity.ok(ApiResponse.success(
                transactions,
                String.format("총 %d건의 재고 이력을 조회했습니다", transactions.size())
        ));
    }

    @GetMapping("/orders/{orderId}/transactions")
    @Operation(summary = "주문별 재고 이력 조회", description = "특정 주문과 관련된 재고 변동 이력을 조회합니다")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getOrderStockTransactions(
            @Parameter(description = "주문 ID", required = true)
            @PathVariable String orderId) {

        log.info("주문별 재고 이력 조회 요청: orderId={}", orderId);

        List<StockTransactionResponse> transactions = inventoryService.getOrderStockTransactions(orderId);

        return ResponseEntity.ok(ApiResponse.success(
                transactions,
                String.format("총 %d건의 재고 이력을 조회했습니다", transactions.size())
        ));
    }

    /**
     * 내부 API - 재고 예약 (주문 서비스에서 호출)
     */
    @PostMapping("/reserve")
    @Operation(summary = "재고 예약", description = "주문 생성 시 재고를 예약합니다 (내부 API)")
    public ResponseEntity<ApiResponse<Void>> reserveStock(
            @Valid @RequestBody StockReservationRequest request,
            @RequestHeader("X-Internal-Token") String internalToken) {

        validateInternalToken(internalToken);

        log.info("재고 예약 요청: productId={}, orderId={}, quantity={}",
                request.getProductId(), request.getOrderId(), request.getQuantity());

        stockReservationService.reserveStock(
                request.getProductId(),
                request.getOrderId(),
                request.getQuantity()
        );

        return ResponseEntity.ok(ApiResponse.success(null, "재고 예약이 완료되었습니다"));
    }

    /**
     * 내부 API - 예약 해제 (주문 취소 시 호출)
     */
    @PostMapping("/release")
    @Operation(summary = "재고 예약 해제", description = "주문 취소 시 예약된 재고를 해제합니다 (내부 API)")
    public ResponseEntity<ApiResponse<Void>> releaseReservation(
            @Valid @RequestBody StockReservationRequest request,
            @RequestHeader("X-Internal-Token") String internalToken) {

        validateInternalToken(internalToken);

        log.info("재고 예약 해제 요청: productId={}, orderId={}, quantity={}",
                request.getProductId(), request.getOrderId(), request.getQuantity());

        stockReservationService.releaseReservation(
                request.getProductId(),
                request.getOrderId(),
                request.getQuantity()
        );

        return ResponseEntity.ok(ApiResponse.success(null, "재고 예약 해제가 완료되었습니다"));
    }

    /**
     * 내부 API - 재고 차감 (결제 완료 시 호출)
     */
    @PostMapping("/decrease")
    @Operation(summary = "재고 차감", description = "결제 완료 시 예약된 재고를 실제로 차감합니다 (내부 API)")
    public ResponseEntity<ApiResponse<Void>> decreaseStock(
            @Valid @RequestBody StockReservationRequest request,
            @RequestHeader("X-Internal-Token") String internalToken) {

        validateInternalToken(internalToken);

        log.info("재고 차감 요청: productId={}, orderId={}, quantity={}",
                request.getProductId(), request.getOrderId(), request.getQuantity());

        stockReservationService.decreaseStock(
                request.getProductId(),
                request.getOrderId(),
                request.getQuantity()
        );

        return ResponseEntity.ok(ApiResponse.success(null, "재고 차감이 완료되었습니다"));
    }

    private void validateInternalToken(String token) {
        if (!"INTERNAL_SECRET_TOKEN".equals(token)) {
            throw new SecurityException("유효하지 않은 내부 API 토큰입니다");
        }
    }
}
