package com.commercium.settlement.service;
import com.commercium.payment.service.PaymentService;
import com.commercium.payment.service.dto.PaymentResponse;
import com.commercium.settlement.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementCalculationService {

    private final PaymentService paymentService; // Payment 도메인과 연동

    /**
     * 정산 항목 계산 및 추가
     */
    public void calculateAndAddItems(Settlement settlement) {
        log.info("정산 항목 계산: settlementId={}, sellerId={}, period={}",
                settlement.getSettlementId().getValue(),
                settlement.getSellerId(),
                settlement.getPeriod().getDescription());

        settlement.startCalculation();

        try {
            // 해당 기간의 완료된 결제들을 조회
            List<String> orderIds = getCompletedOrderIds(settlement.getSellerId(), settlement.getPeriod());

            if (orderIds.isEmpty()) {
                log.info("정산할 주문이 없습니다: sellerId={}", settlement.getSellerId());
                return;
            }

            // 결제 정보 조회
            List<PaymentResponse> payments = paymentService.getPaymentsByOrders(orderIds);

            // 정산 항목 생성
            List<SettlementItem> settlementItems = payments.stream()
                    .flatMap(payment -> createSettlementItemsFromPayment(payment).stream())
                    .toList();

            settlement.addSettlementItems(settlementItems);

            log.info("정산 항목 계산 완료: settlementId={}, itemCount={}, totalAmount={}",
                    settlement.getSettlementId().getValue(),
                    settlementItems.size(),
                    settlement.getAmount().getTotalSales());

        } catch (Exception e) {
            log.error("정산 항목 계산 실패: settlementId={}", settlement.getSettlementId().getValue(), e);
            settlement.fail("정산 계산 중 오류 발생: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 전체 판매자 대상 일일 정산 생성
     */
    public List<Settlement> createDailySettlementsForAllSellers(LocalDate date) {
        log.info("전체 판매자 일일 정산 생성: date={}", date);

        // 실제로는 Seller 도메인에서 활성 판매자 목록 조회
        List<String> activeSellerIds = getActiveSellerIds();

        return activeSellerIds.stream()
                .map(sellerId -> createDailySettlement(sellerId, date))
                .filter(settlement -> settlement.hasItems()) // 정산할 항목이 있는 경우만
                .collect(Collectors.toList());
    }

    private Settlement createDailySettlement(String sellerId, LocalDate date) {
        SettlementPeriod period = SettlementPeriod.daily(date);
        Settlement settlement = Settlement.create(sellerId, period);

        calculateAndAddItems(settlement);

        return settlement;
    }

    private List<SettlementItem> createSettlementItemsFromPayment(PaymentResponse payment) {
        // 실제로는 Order 도메인에서 주문 상세 정보 조회
        // 여기서는 Mock 데이터로 처리

        CommissionRate commissionRate = getSellerCommissionRate(extractSellerIdFromPayment(payment));

        return List.of(
                SettlementItem.create(
                        payment.getOrderId(),
                        "order_item_" + payment.getOrderId(),
                        "product_" + payment.getOrderId(),
                        payment.getPaidAmount(),
                        commissionRate
                )
        );
    }

    private List<String> getCompletedOrderIds(String sellerId, SettlementPeriod period) {
        // 실제로는 Order 도메인에서 완료된 주문 ID 목록 조회
        // Mock 데이터 반환
        return List.of("order1", "order2", "order3");
    }

    private List<String> getActiveSellerIds() {
        // 실제로는 User 도메인에서 활성 판매자 목록 조회
        // Mock 데이터 반환
        return List.of("seller1", "seller2", "seller3");
    }

    private CommissionRate getSellerCommissionRate(String sellerId) {
        // 실제로는 Seller 정보에서 수수료율 조회
        // Mock으로 기본 3% 반환
        return CommissionRate.defaultRate();
    }

    private String extractSellerIdFromPayment(PaymentResponse payment) {
        // 실제로는 Payment → Order → OrderItem → Seller 관계로 조회
        // Mock 데이터 반환
        return "seller_" + payment.getOrderId().substring(0, 3);
    }
}