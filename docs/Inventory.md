```
com.commercium.inventory/
├── controller/
│   └── InventoryController.java           // 재고 조회/관리 API
├── domain/
│   ├── Inventory.java                     // 애그리게이트 루트
│   ├── InventoryTransaction.java          // 재고 트랜잭션 엔티티
│   ├── ProductId.java                     // 값 객체
│   ├── TransactionType.java               // 열거형 (입고/출고/예약/해제)
│   ├── StockQuantity.java                 // 값 객체 (재고 수량)
│   └── InventoryDomainService.java        // 도메인 서비스 (재고 검증)
├── repository/
│   ├── InventoryRepository.java           // 인터페이스
│   ├── InventoryTransactionRepository.java // 트랜잭션 이력 인터페이스
│   └── JpaInventoryRepository.java        // JPA 구현체
├── service/
│   ├── InventoryService.java              // Application Service
│   ├── StockReservationService.java       // 재고 예약 서비스 (Redis Lock)
│   └── dto/
│       ├── StockReservationRequest.java
│       ├── InventoryResponse.java
│       └── StockTransactionResponse.java
├── event/
│   ├── StockReservedEvent.java            // 재고 예약 완료
│   ├── StockReleasedEvent.java            // 재고 해제 완료
│   ├── StockDepletedEvent.java            // 재고 부족 알림
│   ├── InventoryEventListener.java        // 주문 이벤트 수신
│   └── OrderEventConsumer.java            // Kafka 컨슈머
└── infrastructure/
    ├── RedisStockLockManager.java         // Redis 분산락 관리
    ├── InventoryKafkaConsumer.java        // Kafka 메시지 처리
    └── InventoryEventPublisher.java       // 이벤트 발행
```
