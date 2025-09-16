package com.commercium.settlement.batch;

import com.commercium.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final SettlementService settlementService;
    private final DailySettlementJob dailySettlementJob;
    private final WeeklySettlementJob weeklySettlementJob;

    /**
     * 일일 정산 배치 (매일 새벽 2시 실행)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Async("taskExecutor")
    public void runDailySettlement() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        log.info("일일 정산 배치 시작: date={}", yesterday);

        try {
            dailySettlementJob.execute(yesterday);
            log.info("일일 정산 배치 완료: date={}", yesterday);

        } catch (Exception e) {
            log.error("일일 정산 배치 실패: date={}", yesterday, e);
        }
    }

    /**
     * 주간 정산 배치 (매주 월요일 새벽 3시 실행)
     */
    @Scheduled(cron = "0 0 3 * * MON")
    @Async("taskExecutor")
    public void runWeeklySettlement() {
        LocalDate lastWeekStart = LocalDate.now().minusWeeks(1).with(java.time.DayOfWeek.MONDAY);

        log.info("주간 정산 배치 시작: startDate={}", lastWeekStart);

        try {
            weeklySettlementJob.execute(lastWeekStart);
            log.info("주간 정산 배치 완료: startDate={}", lastWeekStart);

        } catch (Exception e) {
            log.error("주간 정산 배치 실패: startDate={}", lastWeekStart, e);
        }
    }

    /**
     * 정산 상태 체크 배치 (1시간마다 실행)
     */
    @Scheduled(fixedRate = 3600000) // 1시간
    @Async("taskExecutor")
    public void checkSettlementStatus() {
        log.debug("정산 상태 체크 배치 실행");

        try {
            // 대기 중인 정산 중 24시간 이상 된 것들 체크
            // 실패한 정산 재시도 등

        } catch (Exception e) {
            log.error("정산 상태 체크 배치 실패", e);
        }
    }
}