com.commercium.settlement/
├── controller/
│   └── SettlementController.java          // 정산 조회/관리 API
├── domain/
│   ├── Settlement.java                    // 애그리게이트 루트
│   ├── SettlementItem.java                // 정산 항목 엔티티
│   ├── SettlementId.java                  // 값 객체
│   ├── SettlementStatus.java              // 열거형 (대기/완료/실패)
│   ├── SettlementPeriod.java              // 값 객체 (정산 기간)
│   ├── CommissionRate.java                // 값 객체 (수수료율)
│   ├── SettlementAmount.java              // 값 객체 (정산 금액)
│   └── SettlementDomainService.java       // 도메인 서비스 (정산 계산)
├── repository/
│   ├── SettlementRepository.java          // 인터페이스
│   ├── SettlementItemRepository.java      // 정산 항목 인터페이스
│   └── JpaSettlementRepository.java       // JPA 구현체
├── service/
│   ├── SettlementService.java             // Application Service
│   ├── SettlementCalculationService.java  // 정산 계산 서비스
│   └── dto/
│       ├── SettlementRequest.java
│       ├── SettlementResponse.java
│       ├── SettlementItemResponse.java
│       └── SettlementReportResponse.java
├── event/
│   ├── SettlementCreatedEvent.java        // 정산 생성
│   ├── SettlementCompletedEvent.java      // 정산 완료
│   ├── SettlementEventListener.java       // 결제 이벤트 수신
│   └── PaymentEventConsumer.java          // Kafka 컨슈머
├── batch/
│   ├── SettlementBatchJobConfig.java      // Spring Batch 설정
│   ├── DailySettlementJob.java            // 일일 정산 배치
│   ├── WeeklySettlementJob.java           // 주간 정산 배치
│   ├── SettlementScheduler.java           // 스케줄러
│   └── SettlementReportGenerator.java     // 정산 리포트 생성
└── infrastructure/
├── SettlementEventPublisher.java      // 이벤트 발행
├── SettlementKafkaConsumer.java       // Kafka 메시지 처리
└── ExcelReportGenerator.java          // 엑셀 리포트 생성
