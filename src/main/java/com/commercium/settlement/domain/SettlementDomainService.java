package com.commercium.settlement.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementDomainService {

    /**
     * 정산 기간 유효성 검증
     */
    public void validateSettlementPeriod(SettlementPeriod period) {
        LocalDate today = LocalDate.now();

        if (period.getEndDate().isAfter(today)) {
            throw new IllegalArgumentException("미래 날짜에 대한 정산은 생성할 수 없습니다");
        }

        if (period.getDays() > 31) {
            throw new IllegalArgumentException("정산 기간은 최대 31일까지 가능합니다");
        }

        log.info("정산 기간 검증 완료: {}", period.getDescription());
    }

    /**
     * 최소 정산 금액 검증
     */
    public boolean isSettlementAmountValid(SettlementAmount amount) {
        BigDecimal minimumAmount = BigDecimal.valueOf(1000); // 최소 1,000원
        return amount.getNetAmount().compareTo(minimumAmount) >= 0;
    }

    /**
     * 수수료율 유효성 검증
     */
    public void validateCommissionRate(CommissionRate rate, String sellerId) {
        if (rate.getPercentage().compareTo(BigDecimal.valueOf(10)) > 0) {
            throw new IllegalArgumentException("수수료율은 10%를 초과할 수 없습니다");
        }

        log.info("수수료율 검증 완료: sellerId={}, rate={}%", sellerId, rate.getPercentage());
    }

    /**
     * 정산 완료 가능 여부 검증
     */
    public boolean canCompleteSettlement(Settlement settlement) {
        return settlement.hasItems() &&
                settlement.getStatus().isCompletable() &&
                settlement.getAmount().getNetAmount().compareTo(BigDecimal.ZERO) > 0;
    }
}
