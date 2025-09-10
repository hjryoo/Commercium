package com.commercium.settlement.domain;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class SettlementPeriod {

    private LocalDate startDate;
    private LocalDate endDate;

    protected SettlementPeriod() {} // JPA용

    public static SettlementPeriod of(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        return new SettlementPeriod(startDate, endDate);
    }

    public static SettlementPeriod daily(LocalDate date) {
        return new SettlementPeriod(date, date);
    }

    public static SettlementPeriod weekly(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        return new SettlementPeriod(startDate, endDate);
    }

    public static SettlementPeriod monthly(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return new SettlementPeriod(startDate, endDate);
    }

    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public String getDescription() {
        if (startDate.equals(endDate)) {
            return startDate.toString();
        }
        return startDate + " ~ " + endDate;
    }

    private static void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("정산 시작일과 종료일은 필수입니다");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("정산 시작일은 종료일보다 이후일 수 없습니다");
        }
    }
}
