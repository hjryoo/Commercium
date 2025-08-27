## Commercium

### DOCS 
[Config](docs/config.md)
[Order](docs/order.md)


🏗️ 핵심 기능
주문 (Order)

    장바구니 → 주문 생성/취소

    다수 상품 지원

    주문 상태 전이: PLACED → PAID → SHIPPED

재고 (Inventory)

    주문 시 재고 차감, 취소 시 보정

    품절 알림 이벤트 발행

결제 (Payment)

    카드·가상계좌 시뮬레이터 (외부 REST API Mock)

    결제 상태에 따른 주문 상태 이벤트 연동

정산 (Settlement)

    일간/주간 매출 합산 → 판매자 정산

    PG 수수료 차감, VAT 반영

    정산 리포트 및 엑셀 다운로드

운영·UX

    관리자용 대시보드 (Spring Boot + Thymeleaf 또는 React + Material UI)

    RESTful API / gRPC 기반 API 제공

🛠️ 기술 스택

    백엔드: Spring Boot 3.x (Java 21)

    DB: PostgreSQL (RDB, FK·인덱스 설계) + Redis (캐싱·재고 race-condition 방지)

    메시지 큐: Kafka 또는 RabbitMQ (비동기 주문·결제·정산 이벤트 처리)

    API 문서화: Springdoc OpenAPI (Swagger UI)

    인증·보안: Spring Security + JWT (판매자/사용자 역할 분리)

    테스트: JUnit 5 + Testcontainers (실DB 통합 테스트)

    Infra/DevOps: Docker Compose, GitHub Actions CI, Dockerfile

    프론트 (Optional): React + Material UI (관리자 대시보드)

📐 아키텍처 포인트

    Domain-Driven Design (DDD)

        모듈별 분리: order, inventory, payment, settlement

        도메인 이벤트 기반 의존도 최소화

    CQRS Lite

        조회는 캐시/리플리카 DB, 쓰기는 트랜잭션 DB

    재고 Race-Condition 방지

        Redis + Lua Script 또는 Redisson Lock

    Batch & Streaming

        Scheduler 기반 정산 배치

        실시간 주문 이벤트 스트리밍