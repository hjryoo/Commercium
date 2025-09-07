```
com.commercium.order/
├── controller/
│   └── OrderController.java
├── domain/
│   ├── Order.java                 // 애그리게이트 루트
│   ├── OrderItem.java            // 엔티티
│   ├── OrderId.java              // 값 객체
│   ├── OrderStatus.java          // 열거형
│   ├── ShippingAddress.java      // 값 객체
│   └── OrderDomainService.java   // 도메인 서비스
├── repository/
│   ├── OrderRepository.java      // 인터페이스
│   └── JpaOrderRepository.java   // JPA 구현체
├── service/
│   ├── OrderService.java         // Application Service
│   └── dto/
│       ├── CreateOrderRequest.java
│       ├── OrderResponse.java
│       └── OrderItemResponse.java
├── event/
│   ├── OrderCreatedEvent.java
│   ├── OrderCancelledEvent.java
│   ├── OrderPaidEvent.java
│   └── OrderEventListener.java
└── infrastructure/
├── JpaOrderRepositoryImpl.java
└── OrderEventPublisher.java
```
