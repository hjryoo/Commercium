package com.commercium.settlement.infrastructure;

import com.commercium.settlement.service.SettlementService;
import com.commercium.settlement.service.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelReportGenerator {

    private final SettlementService settlementService;

    /**
     * 일일 정산 리포트 생성
     */
    public byte[] generateDailySettlementReport(LocalDate date, List<SettlementResponse> settlements) {
        log.info("일일 정산 엑셀 리포트 생성: date={}, count={}", date, settlements.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("일일 정산 리포트");

            // 헤더 스타일
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            int rowNum = 0;

            // 제목
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("일일 정산 리포트 - " + date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
            titleCell.setCellStyle(headerStyle);

            rowNum++; // 빈 줄

            // 컬럼 헤더
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"정산ID", "판매자ID", "기간", "총매출", "수수료", "VAT", "정산금액", "상태", "생성일시"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 행
            for (SettlementResponse settlement : settlements) {
                Row dataRow = sheet.createRow(rowNum++);

                dataRow.createCell(0).setCellValue(settlement.getSettlementId());
                dataRow.createCell(1).setCellValue(settlement.getSellerId());
                dataRow.createCell(2).setCellValue(settlement.getPeriodDescription());

                Cell totalSalesCell = dataRow.createCell(3);
                totalSalesCell.setCellValue(settlement.getTotalSales().doubleValue());
                totalSalesCell.setCellStyle(numberStyle);

                Cell commissionCell = dataRow.createCell(4);
                commissionCell.setCellValue(settlement.getCommissionAmount().doubleValue());
                commissionCell.setCellStyle(numberStyle);

                Cell vatCell = dataRow.createCell(5);
                vatCell.setCellValue(settlement.getVatAmount().doubleValue());
                vatCell.setCellStyle(numberStyle);

                Cell netAmountCell = dataRow.createCell(6);
                netAmountCell.setCellValue(settlement.getNetAmount().doubleValue());
                netAmountCell.setCellStyle(numberStyle);

                dataRow.createCell(7).setCellValue(settlement.getStatusDescription());
                dataRow.createCell(8).setCellValue(settlement.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return writeWorkbookToBytes(workbook);

        } catch (IOException e) {
            log.error("일일 정산 엑셀 리포트 생성 실패: date={}", date, e);
            throw new RuntimeException("엑셀 리포트 생성 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 주간 정산 리포트 생성
     */
    public byte[] generateWeeklySettlementReport(LocalDate startDate, LocalDate endDate) {
        log.info("주간 정산 엑셀 리포트 생성: {} ~ {}", startDate, endDate);

        // 해당 기간의 모든 정산 조회
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 실제로는 기간별 정산 조회 메서드 구현 필요
        List<SettlementResponse> settlements = List.of(); // Mock

        return generateDailySettlementReport(startDate, settlements); // 같은 형식 사용
    }

    /**
     * 판매자별 정산 리포트 생성
     */
    public byte[] generateSellerSettlementReport(String sellerId, LocalDate startDate, LocalDate endDate) {
        log.info("판매자별 정산 엑셀 리포트 생성: sellerId={}, {} ~ {}", sellerId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<SettlementResponse> settlements = settlementService.getSellerSettlements(sellerId, startDateTime, endDateTime);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("판매자 정산 리포트");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            int rowNum = 0;

            // 제목
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(String.format("판매자 정산 리포트 - %s (%s ~ %s)",
                    sellerId, startDate, endDate));
            titleCell.setCellStyle(headerStyle);

            rowNum++; // 빈 줄

            // 요약 정보
            BigDecimal totalSales = settlements.stream()
                    .map(SettlementResponse::getTotalSales)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalNet = settlements.stream()
                    .map(SettlementResponse::getNetAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Row summaryRow1 = sheet.createRow(rowNum++);
            summaryRow1.createCell(0).setCellValue("총 정산 건수:");
            summaryRow1.createCell(1).setCellValue(settlements.size());

            Row summaryRow2 = sheet.createRow(rowNum++);
            summaryRow2.createCell(0).setCellValue("총 매출액:");
            Cell totalSalesCell = summaryRow2.createCell(1);
            totalSalesCell.setCellValue(totalSales.doubleValue());
            totalSalesCell.setCellStyle(numberStyle);

            Row summaryRow3 = sheet.createRow(rowNum++);
            summaryRow3.createCell(0).setCellValue("총 정산액:");
            Cell totalNetCell = summaryRow3.createCell(1);
            totalNetCell.setCellValue(totalNet.doubleValue());
            totalNetCell.setCellStyle(numberStyle);

            rowNum++; // 빈 줄

            // 상세 데이터 (기존과 동일한 로직)
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"정산ID", "기간", "총매출", "수수료", "VAT", "정산금액", "상태", "완료일시"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 행 추가 (앞서와 유사한 로직)
            for (SettlementResponse settlement : settlements) {
                Row dataRow = sheet.createRow(rowNum++);

                dataRow.createCell(0).setCellValue(settlement.getSettlementId());
                dataRow.createCell(1).setCellValue(settlement.getPeriodDescription());

                Cell salesCell = dataRow.createCell(2);
                salesCell.setCellValue(settlement.getTotalSales().doubleValue());
                salesCell.setCellStyle(numberStyle);

                Cell commissionCell = dataRow.createCell(3);
                commissionCell.setCellValue(settlement.getCommissionAmount().doubleValue());
                commissionCell.setCellStyle(numberStyle);

                Cell vatCell = dataRow.createCell(4);
                vatCell.setCellValue(settlement.getVatAmount().doubleValue());
                vatCell.setCellStyle(numberStyle);

                Cell netCell = dataRow.createCell(5);
                netCell.setCellValue(settlement.getNetAmount().doubleValue());
                netCell.setCellStyle(numberStyle);

                dataRow.createCell(6).setCellValue(settlement.getStatusDescription());

                String completedAt = settlement.getCompletedAt() != null ?
                        settlement.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-";
                dataRow.createCell(7).setCellValue(completedAt);
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return writeWorkbookToBytes(workbook);

        } catch (IOException e) {
            log.error("판매자 정산 엑셀 리포트 생성 실패: sellerId={}", sellerId, e);
            throw new RuntimeException("엑셀 리포트 생성 중 오류가 발생했습니다", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private byte[] writeWorkbookToBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}