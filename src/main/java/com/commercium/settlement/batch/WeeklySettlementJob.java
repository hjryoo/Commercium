package com.commercium.settlement.batch;

import com.commercium.settlement.domain.SettlementStatus;
import com.commercium.settlement.service.SettlementService;
import com.commercium.settlement.service.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklySettlementJob {

    private final SettlementService settlementService;
    private final SettlementReportGenerator reportGenerator;

    public void execute(LocalDate weekStartDate) {
        log.info("주간 정산 작업 시작: weekStartDate={}", weekStartDate);

        LocalDate weekEndDate = weekStartDate.plusDays(6);

        try {
            // 1. 해당 주의 모든 판매자 정산 조회
            LocalDateTime startDateTime = weekStartDate.atStartOfDay();
            LocalDateTime endDateTime = weekEndDate.atTime(23, 59, 59);

            // 활성 판매자 목록 조회 (실제로는 User 도메인에서)
            List<String> activeSellerIds = getActiveSellerIds();

            // 2. 판매자별 주간 정산 생성
            for (String sellerId : activeSellerIds) {
                try {
                    // 기존에 해당 기간 정산이 있는지 확인
                    List<SettlementResponse> existingSettlements =
                            settlementService.getSellerSettlements(sellerId, startDateTime, endDateTime);

                    if (existingSettlements.isEmpty()) {
                        // 주간 정산 생성
                        SettlementResponse weeklySettlement =
                                settlementService.createSettlement(sellerId, weekStartDate, weekEndDate);

                        // 완료 처리
                        settlementService.completeSettlement(weeklySettlement.getSettlementId());

                        log.info("주간 정산 완료: sellerId={}, settlementId={}",
                                sellerId, weeklySettlement.getSettlementId());
                    }

                } catch (Exception e) {
                    log.error("판매자 주간 정산 실패: sellerId={}, week={}", sellerId, weekStartDate, e);
                }
            }

            // 3. 주간 정산 리포트 생성
            reportGenerator.generateWeeklyReport(weekStartDate, weekEndDate);

            log.info("주간 정산 작업 완료: week={} ~ {}", weekStartDate, weekEndDate);

        } catch (Exception e) {
            log.error("주간 정산 작업 실패: weekStartDate={}", weekStartDate, e);
            throw e;
        }
    }

    private List<String> getActiveSellerIds() {
        // 실제로는 User 도메인에서 활성 판매자 목록 조회
        return List.of("seller1", "seller2", "seller3");
    }
}