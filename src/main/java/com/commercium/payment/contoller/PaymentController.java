package com.commercium.payment.contoller;

import com.commercium.common.dto.ApiResponse;
import com.commercium.payment.service.PaymentService;
import com.commercium.payment.service.dto.PaymentRequest;
import com.commercium.payment.service.dto.PaymentResponse;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "결제 관리", description = "결제 요청, 조회, 취소 등의 기능을 제공합니다")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "결제 요청", description = "새로운 결제를 요청합니다")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentRequest request) {

        String userId = jwt.getSubject();
        log.info("결제 요청: userId={}, orderId={}, amount={}",
                userId, request.getOrderId(), request.getAmount());

        PaymentResponse response = paymentService.createPayment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "결제 요청이 완료되었습니다"));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "결제 조회", description = "결제 정보를 조회합니다")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @Parameter(description = "결제 ID", required = true)
            @PathVariable String paymentId) {

        log.info("결제 조회 요청: paymentId={}", paymentId);

        PaymentResponse response = paymentService.getPayment(paymentId);

        return ResponseEntity.ok(ApiResponse.success(response, "결제 정보를 조회했습니다"));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "주문별 결제 조회", description = "특정 주문의 결제 정보를 조회합니다")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(
            @Parameter(description = "주문 ID", required = true)
            @PathVariable String orderId) {

        log.info("주문별 결제 조회 요청: orderId={}", orderId);

        PaymentResponse response = paymentService.getPaymentByOrder(orderId);

        return ResponseEntity.ok(ApiResponse.success(response, "결제 정보를 조회했습니다"));
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "결제 취소", description = "결제를 취소합니다")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @Parameter(description = "결제 ID", required = true)
            @PathVariable String paymentId,
            @Parameter(description = "취소 금액", required = true)
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal cancelAmount,
            @Parameter(description = "취소 사유")
            @RequestParam(required = false, defaultValue = "고객 요청") String reason) {

        log.info("결제 취소 요청: paymentId={}, cancelAmount={}, reason={}",
                paymentId, cancelAmount, reason);

        PaymentResponse response = paymentService.cancelPayment(paymentId, cancelAmount, reason);

        return ResponseEntity.ok(ApiResponse.success(response, "결제 취소가 완료되었습니다"));
    }

    /**
     * PG사 콜백 API - 결제 완료
     */
    @PostMapping("/callback/complete")
    @Operation(summary = "결제 완료 콜백", description = "PG사에서 호출하는 결제 완료 콜백 API")
    public ResponseEntity<ApiResponse<Void>> paymentCompleteCallback(
            @RequestParam String paymentKey,
            @RequestParam BigDecimal amount) {

        log.info("결제 완료 콜백: paymentKey={}, amount={}", paymentKey, amount);

        paymentService.completePayment(paymentKey, amount);

        return ResponseEntity.ok(ApiResponse.success(null, "결제 완료 처리되었습니다"));
    }

    /**
     * PG사 콜백 API - 결제 실패
     */
    @PostMapping("/callback/fail")
    @Operation(summary = "결제 실패 콜백", description = "PG사에서 호출하는 결제 실패 콜백 API")
    public ResponseEntity<ApiResponse<Void>> paymentFailCallback(
            @RequestParam String paymentKey,
            @RequestParam String errorCode,
            @RequestParam String errorMessage) {

        log.info("결제 실패 콜백: paymentKey={}, errorCode={}, errorMessage={}",
                paymentKey, errorCode, errorMessage);

        String reason = String.format("[%s] %s", errorCode, errorMessage);
        paymentService.failPayment(paymentKey, reason);

        return ResponseEntity.ok(ApiResponse.success(null, "결제 실패 처리되었습니다"));
    }
}