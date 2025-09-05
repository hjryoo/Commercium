com.commercium.payment/
├── controller/
│   └── PaymentController.java             // 결제 요청/조회 API
├── domain/
│   ├── Payment.java                       // 애그리게이트 루트
│   ├── PaymentId.java                     // 값 객체
│   ├── PaymentStatus.java                 // 열거형 (대기/완료/실패/취소)
│   ├── PaymentMethod.java                 // 열거형 (카드/계좌이체/가상계좌)
│   ├── PaymentAmount.java                 // 값 객체 (금액)
│   ├── PaymentProvider.java               // 값 객체 (결제 대행사)
│   └── PaymentDomainService.java          // 도메인 서비스 (결제 검증)
├── repository/
│   ├── PaymentRepository.java             // 인터페이스
│   └── JpaPaymentRepository.java          // JPA 구현체
├── service/
│   ├── PaymentService.java                // Application Service
│   ├── PaymentProcessingService.java      // 결제 처리 서비스
│   └── dto/
│       ├── PaymentRequest.java
│       ├── PaymentResponse.java
│       └── PaymentCallbackRequest.java
├── event/
│   ├── PaymentCompletedEvent.java         // 결제 완료
│   ├── PaymentFailedEvent.java            // 결제 실패
│   ├── PaymentCancelledEvent.java         // 결제 취소
│   ├── PaymentEventListener.java          // 주문 이벤트 수신
│   └── OrderEventConsumer.java            // Kafka 컨슈머
├── external/
│   ├── PgClient.java                      // PG사 연동 인터페이스
│   ├── MockPgClient.java                  // Mock PG 구현체
│   ├── TossPaymentClient.java             // 토스페이먼츠 구현체
│   └── dto/
│       ├── PgPaymentRequest.java
│       └── PgPaymentResponse.java
└── infrastructure/
├── PaymentEventPublisher.java         // 이벤트 발행
└── PaymentKafkaConsumer.java          // Kafka 메시지 처리
