package com.commercium.settlement.batch;

import com.commercium.settlement.service.SettlementService;
import com.commercium.settlement.service.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailySettlementJob {

    private final SettlementService settlementService;
    private final SettlementReportGenerator reportGenerator;

    public void execute(LocalDate date) {
        log.info("일일 정산 작업 시작: date={}", date);

        try {
            // 1. 일일 정산 생성
            List<SettlementResponse> settlements = settlementService.createDailySettlements(date);

            log.info("일일 정산 생성 완료: date={}, settlementCount={}", date, settlements.size());

            // 2. 생성된 정산들 완료 처리
            int completedCount = 0;
            for (SettlementResponse settlement : settlements) {
                try {
                    settlementService.completeSettlement(settlement.getSettlementId());
                    completedCount++;

                } catch (Exception e) {
                    log.error("정산 완료 처리 실패: settlementId={}", settlement.getSettlementId(), e);
                }
            }

            log.info("일일 정산 완료 처리: date={}, completedCount={}/{}",
                    date, completedCount, settlements.size());

            // 3. 일일 정산 리포트 생성
            if (!settlements.isEmpty()) {
                reportGenerator.generateDailyReport(date, settlements);
            }

        } catch (Exception e) {
            log.error("일일 정산 작업 실패: date={}", date, e);
            throw e;
        }
    }
}
