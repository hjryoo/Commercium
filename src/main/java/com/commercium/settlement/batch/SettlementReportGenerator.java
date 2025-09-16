package com.commercium.settlement.batch;

import com.commercium.settlement.infrastructure.ExcelReportGenerator;
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
public class SettlementReportGenerator {

    private final SettlementService settlementService;
    private final ExcelReportGenerator excelGenerator;

    /**
     * 일일 정산 리포트 생성
     */
    public void generateDailyReport(LocalDate date, List<SettlementResponse> settlements) {
        log.info("일일 정산 리포트 생성: date={}", date);

        try {
            String fileName = String.format("daily_settlement_%s.xlsx", date);
            byte[] excelData = excelGenerator.generateDailySettlementReport(date, settlements);

            // 실제로는 파일 저장소(S3 등)에 저장
            saveReportFile(fileName, excelData);

            log.info("일일 정산 리포트 생성 완료: fileName={}", fileName);

        } catch (Exception e) {
            log.error("일일 정산 리포트 생성 실패: date={}", date, e);
        }
    }

    /**
     * 주간 정산 리포트 생성
     */
    public void generateWeeklyReport(LocalDate startDate, LocalDate endDate) {
        log.info("주간 정산 리포트 생성: {} ~ {}", startDate, endDate);

        try {
            String fileName = String.format("weekly_settlement_%s_%s.xlsx", startDate, endDate);
            byte[] excelData = excelGenerator.generateWeeklySettlementReport(startDate, endDate);

            saveReportFile(fileName, excelData);

            log.info("주간 정산 리포트 생성 완료: fileName={}", fileName);

        } catch (Exception e) {
            log.error("주간 정산 리포트 생성 실패: {} ~ {}", startDate, endDate, e);
        }
    }

    /**
     * 판매자별 정산 리포트 생성
     */
    public byte[] generateSellerReport(String sellerId, LocalDate startDate, LocalDate endDate) {
        log.info("판매자 정산 리포트 생성: sellerId={}, {} ~ {}", sellerId, startDate, endDate);

        try {
            return excelGenerator.generateSellerSettlementReport(sellerId, startDate, endDate);

        } catch (Exception e) {
            log.error("판매자 정산 리포트 생성 실패: sellerId={}", sellerId, e);
            throw e;
        }
    }

    private void saveReportFile(String fileName, byte[] data) {
        // 실제로는 파일 저장소에 저장
        log.info("리포트 파일 저장: fileName={}, size={} bytes", fileName, data.length);
    }
}