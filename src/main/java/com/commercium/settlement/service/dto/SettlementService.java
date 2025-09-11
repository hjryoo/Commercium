package com.commercium.settlement.service.dto;

import com.commercium.common.exception.BusinessRuleViolationException;
import com.commercium.settlement.domain.*;
import com.commercium.settlement.repository.SettlementRepository;
import com.commercium.settlement.service.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementCalculationService calculationService;

    /**
     * 정산 생성 (판매자별, 기간별)
     */
    public SettlementResponse createSettlement(String sellerId, LocalDate startDate, LocalDate endDate) {
        log.info("정산 생성 요청: sellerId={}, period={}~{}", sellerId, startDate, endDate);

        SettlementPeriod period = SettlementPeriod.of(startDate, endDate);

        // 중복 정산 확인
        if (settlementRepository.existsBySellerIdAndPeriod(sellerId, startDate, endDate)) {
            throw new BusinessRuleViolationException("이미 해당 기간의 정산이 존재합니다");
        }

        // 정산 생성
        Settlement settlement = Settlement.create(sellerId, period);

        // 정산 항목 계산 및 추가
        calculationService.calculateAndAddItems(settlement);

        Settlement savedSettlement = settlementRepository.save(settlement);

        log.info("정산 생성 완료: settlementId={}, itemCount={}",
                savedSettlement.getSettlementId().getValue(), savedSettlement.getItemCount());

        return SettlementResponse.from(savedSettlement);
    }

    /**
     * 일일 정산 생성 (배치에서 호출)
     */
    public List<SettlementResponse> createDailySettlements(LocalDate date) {
        log.info("일일 정산 생성: date={}", date);

        return calculationService.createDailySettlementsForAllSellers(date)
                .stream()
                .map(settlement -> {
                    Settlement saved = settlementRepository.save(settlement);
                    return SettlementResponse.fromWithoutItems(saved);
                })
                .toList();
    }

    /**
     * 정산 완료 처리
     */
    public SettlementResponse completeSettlement(String settlementId) {
        log.info("정산 완료 처리: settlementId={}", settlementId);

        Settlement settlement = settlementRepository.findById(SettlementId.of(settlementId))
                .orElseThrow(() -> new BusinessRuleViolationException("정산 정보를 찾을 수 없습니다"));

        if (!settlement.hasItems()) {
            throw new BusinessRuleViolationException("정산할 항목이 없습니다");
        }

        settlement.complete();
        Settlement savedSettlement = settlementRepository.save(settlement);

        log.info("정산 완료: settlementId={}, netAmount={}",
                settlementId, savedSettlement.getAmount().getNetAmount());

        return SettlementResponse.from(savedSettlement);
    }

    /**
     * 정산 조회
     */
    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findById(SettlementId.of(settlementId))
                .orElseThrow(() -> new BusinessRuleViolationException("정산 정보를 찾을 수 없습니다"));

        return SettlementResponse.from(settlement);
    }

    /**
     * 판매자별 정산 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSellerSettlements(String sellerId) {
        return settlementRepository.findBySellerId(sellerId)
                .stream()
                .map(SettlementResponse::fromWithoutItems)
                .toList();
    }

    /**
     * 판매자별 기간별 정산 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSellerSettlements(String sellerId,
                                                         LocalDateTime startDateTime,
                                                         LocalDateTime endDateTime) {
        return settlementRepository.findBySellerIdAndSettlementDateBetween(sellerId, startDateTime, endDateTime)
                .stream()
                .map(SettlementResponse::fromWithoutItems)
                .toList();
    }

    /**
     * 상태별 정산 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByStatus(SettlementStatus status) {
        return settlementRepository.findByStatus(status)
                .stream()
                .map(SettlementResponse::fromWithoutItems)
                .toList();
    }

    /**
     * 정산 취소
     */
    public void cancelSettlement(String settlementId, String reason) {
        log.info("정산 취소: settlementId={}, reason={}", settlementId, reason);

        Settlement settlement = settlementRepository.findById(SettlementId.of(settlementId))
                .orElseThrow(() -> new BusinessRuleViolationException("정산 정보를 찾을 수 없습니다"));

        settlement.cancel(reason);
        settlementRepository.save(settlement);

        log.info("정산 취소 완료: settlementId={}", settlementId);
    }
}

