package com.commercium.order.controller;

import com.commercium.common.dto.ApiResponse;
import com.commercium.order.service.OrderService;
import com.commercium.order.service.dto.CreateOrderRequest;
import com.commercium.order.service.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "주문 관리", description = "주문 생성, 조회, 취소 등의 기능을 제공합니다")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrderRequest request) {

        String userId = jwt.getSubject();
        log.info("주문 생성 요청: userId={}, itemCount={}", userId, request.getItems().size());

        OrderResponse orderResponse = orderService.createOrder(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        orderResponse,
                        "주문이 성공적으로 생성되었습니다"
                ));
    }

    @GetMapping
    @Operation(summary = "내 주문 목록 조회", description = "사용자의 모든 주문을 조회합니다")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("주문 목록 조회 요청: userId={}", userId);

        List<OrderResponse> orders = orderService.getUserOrders(userId);

        return ResponseEntity.ok(ApiResponse.success(
                orders,
                String.format("총 %d개의 주문을 조회했습니다", orders.size())
        ));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable String orderId) {

        String userId = jwt.getSubject();
        log.info("주문 상세 조회 요청: userId={}, orderId={}", userId, orderId);

        OrderResponse order = orderService.getOrder(userId, orderId);

        return ResponseEntity.ok(ApiResponse.success(
                order,
                "주문 정보를 성공적으로 조회했습니다"
        ));
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다 (결제 완료 전까지만 가능)")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable String orderId) {

        String userId = jwt.getSubject();
        log.info("주문 취소 요청: userId={}, orderId={}", userId, orderId);

        orderService.cancelOrder(userId, orderId);

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "주문이 성공적으로 취소되었습니다"
        ));
    }

    /**
     * 결제 시스템에서 호출하는 내부 API
     * 외부에서 직접 호출할 수 없도록 보안 처리 필요
     */
    @PatchMapping("/{orderId}/confirm-payment")
    @Operation(summary = "결제 확인 처리", description = "결제 완료 시 주문 상태를 업데이트합니다 (내부 API)")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @Parameter(description = "주문 ID", required = true)
            @PathVariable String orderId,
            @RequestHeader("X-Internal-Token") String internalToken) {

        // 내부 API 토큰 검증 로직 (실제로는 별도 인터셉터에서 처리)
        validateInternalToken(internalToken);

        log.info("결제 확인 처리 요청: orderId={}", orderId);

        orderService.confirmPayment(orderId);

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "결제 확인 처리가 완료되었습니다"
        ));
    }

    private void validateInternalToken(String token) {
        // 실제 구현에서는 설정된 내부 토큰과 비교
        if (!"INTERNAL_SECRET_TOKEN".equals(token)) {
            throw new SecurityException("유효하지 않은 내부 API 토큰입니다");
        }
    }
}

