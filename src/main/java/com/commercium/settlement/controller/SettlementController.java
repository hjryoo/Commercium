package com.commercium.settlement.controller;
import com.commercium.common.dto.ApiResponse;
import com.commercium.settlement.batch.SettlementReportGenerator;
import com.commercium.settlement.domain.SettlementStatus;
import com.commercium.settlement.service.SettlementService;
import com.commercium.settlement.service.dto.SettlementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "정산 관리", description = "정산 생성, 조회, 리포트 다운로드 등의 기능을 제공합니다")
@SecurityRequirement(name = "bearerAuth")
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementReportGenerator reportGenerator;

    @PostMapping
    @Operation(summary = "정산 생성", description = "특정 기간의 정산을 생성합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettlementResponse>> createSettlement(
            @Parameter(description = "판매자 ID", required = true)
            @RequestParam String sellerId,
            @Parameter(description = "시작일", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("정산 생성 요청: sellerId={}, period={}~{}", sellerId, startDate, endDate);

        SettlementResponse response = settlementService.createSettlement(sellerId, startDate, endDate);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "정산이 성공적으로 생성되었습니다"));
    }

    @GetMapping("/{settlementId}")
    @Operation(summary = "정산 조회", description = "정산 상세 정보를 조회합니다")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SettlementResponse>> getSettlement(
            @Parameter(description = "정산 ID", required = true)
            @PathVariable String settlementId) {

        log.info("정산 조회 요청: settlementId={}", settlementId);

        SettlementResponse response = settlementService.getSettlement(settlementId);

        return ResponseEntity.ok(ApiResponse.success(response, "정산 정보를 조회했습니다"));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "판매자별 정산 목록 조회", description = "특정 판매자의 정산 목록을 조회합니다")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and #sellerId == authentication.subject)")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSellerSettlements(
            @Parameter(description = "판매자 ID", required = true)
            @PathVariable String sellerId,
            @Parameter(description = "시작일시")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @Parameter(description = "종료일시")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {

        log.info("판매자별 정산 목록 조회: sellerId={}, period={}~{}", sellerId, startDateTime, endDateTime);

        List<SettlementResponse> settlements;
        if (startDateTime != null && endDateTime != null) {
            settlements = settlementService.getSellerSettlements(sellerId, startDateTime, endDateTime);
        } else {
            settlements = settlementService.getSellerSettlements(sellerId);
        }

        return ResponseEntity.ok(ApiResponse.success(
                settlements,
                String.format("총 %d건의 정산을 조회했습니다", settlements.size())
        ));
    }

    @GetMapping("/my")
    @Operation(summary = "내 정산 목록 조회", description = "로그인한 판매자의 정산 목록을 조회합니다")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getMySettlements(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "시작일시")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @Parameter(description = "종료일시")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {

        String sellerId = jwt.getSubject();
        log.info("내 정산 목록 조회: sellerId={}", sellerId);

        List<SettlementResponse> settlements;
        if (startDateTime != null && endDateTime != null) {
            settlements = settlementService.getSellerSettlements(sellerId, startDateTime, endDateTime);
        } else {
            settlements = settlementService.getSellerSettlements(sellerId);
        }

        return ResponseEntity.ok(ApiResponse.success(
                settlements,
                String.format("총 %d건의 정산을 조회했습니다", settlements.size())
        ));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "상태별 정산 조회", description = "특정 상태의 정산 목록을 조회합니다 (관리자용)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByStatus(
            @Parameter(description = "정산 상태", required = true)
            @PathVariable SettlementStatus status) {

        log.info("상태별 정산 조회: status={}", status);

        List<SettlementResponse> settlements = settlementService.getSettlementsByStatus(status);

        return ResponseEntity.ok(ApiResponse.success(
                settlements,
                String.format("상태 %s인 정산 %d건을 조회했습니다", status.getDescription(), settlements.size())
        ));
    }

    @PatchMapping("/{settlementId}/complete")
    @Operation(summary = "정산 완료 처리", description = "대기 중인 정산을 완료 처리합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettlementResponse>> completeSettlement(
            @Parameter(description = "정산 ID", required = true)
            @PathVariable String settlementId) {

        log.info("정산 완료 처리 요청: settlementId={}", settlementId);

        SettlementResponse response = settlementService.completeSettlement(settlementId);

        return ResponseEntity.ok(ApiResponse.success(response, "정산이 완료되었습니다"));
    }

    @PatchMapping("/{settlementId}/cancel")
    @Operation(summary = "정산 취소", description = "정산을 취소합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelSettlement(
            @Parameter(description = "정산 ID", required = true)
            @PathVariable String settlementId,
            @Parameter(description = "취소 사유")
            @RequestParam(required = false, defaultValue = "관리자 취소") String reason) {

        log.info("정산 취소 요청: settlementId={}, reason={}", settlementId, reason);

        settlementService.cancelSettlement(settlementId, reason);

        return ResponseEntity.ok(ApiResponse.success(null, "정산이 취소되었습니다"));
    }

    @GetMapping("/reports/seller/{sellerId}/download")
    @Operation(summary = "판매자 정산 리포트 다운로드", description = "판매자별 정산 리포트를 엑셀 파일로 다운로드합니다")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and #sellerId == authentication.subject)")
    public ResponseEntity<byte[]> downloadSellerReport(
            @Parameter(description = "판매자 ID", required = true)
            @PathVariable String sellerId,
            @Parameter(description = "시작일", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("판매자 정산 리포트 다운로드: sellerId={}, period={}~{}", sellerId, startDate, endDate);

        byte[] reportData = reportGenerator.generateSellerReport(sellerId, startDate, endDate);

        String fileName = String.format("settlement_report_%s_%s_%s.xlsx", sellerId, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(reportData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
    }

    @GetMapping("/reports/my/download")
    @Operation(summary = "내 정산 리포트 다운로드", description = "로그인한 판매자의 정산 리포트를 엑셀 파일로 다운로드합니다")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<byte[]> downloadMyReport(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "시작일", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String sellerId = jwt.getSubject();
        log.info("내 정산 리포트 다운로드: sellerId={}, period={}~{}", sellerId, startDate, endDate);

        byte[] reportData = reportGenerator.generateSellerReport(sellerId, startDate, endDate);

        String fileName = String.format("my_settlement_report_%s_%s.xlsx", startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(reportData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
    }
}