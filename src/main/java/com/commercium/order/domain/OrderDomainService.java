package com.commercium.order.domain;

import com.commercium.common.exception.BusinessRuleViolationException;
import com.commercium.order.service.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDomainService {

    // 실제로는 ProductService나 외부 API를 통해 상품 정보를 조회

    public List<OrderItem> validateAndCreateOrderItems(List<CreateOrderRequest.OrderItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(this::createOrderItem)
                .toList();
    }

    private OrderItem createOrderItem(CreateOrderRequest.OrderItemRequest itemRequest) {
        // 실제로는 상품 정보를 조회해서 가격, 판매자 정보 등을 가져옴
        // 여기서는 Mock 데이터로 처리

        String productId = itemRequest.getProductId();
        String sellerId = "seller-" + productId; // Mock
        String productName = "상품명-" + productId; // Mock
        BigDecimal unitPrice = BigDecimal.valueOf(10000); // Mock

        validateProductAvailability(productId, itemRequest.getQuantity());

        return OrderItem.create(productId, sellerId, productName, itemRequest.getQuantity(), unitPrice);
    }

    private void validateProductAvailability(String productId, Integer quantity) {
        // 실제로는 재고 서비스를 호출해서 재고 확인
        // 여기서는 간단한 비즈니스 규칙만 적용

        if (quantity > 10) {
            throw new BusinessRuleViolationException(
                    String.format("상품 %s는 최대 10개까지만 주문 가능합니다", productId)
            );
        }

        log.info("상품 주문 가능 여부 확인 완료: productId={}, quantity={}", productId, quantity);
    }
}