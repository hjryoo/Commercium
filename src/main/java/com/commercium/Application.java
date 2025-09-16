package com.commercium;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Commercium - 미니 커머스 플랫폼
 *
 * 주문, 재고, 결제, 정산 기능을 포함한 이커머스 백엔드 시스템
 *
 * 주요 기능:
 * - 주문 관리 (Order Domain)
 * - 재고 관리 (Inventory Domain)
 * - 결제 처리 (Payment Domain)
 * - 정산 관리 (Settlement Domain)
 *
 * 기술 스택:
 * - Spring Boot 3.x + Java 21
 * - PostgreSQL + Redis
 * - Kafka + RabbitMQ
 * - Docker + Docker Compose
 *
 * 아키텍처 패턴:
 * - Domain-Driven Design (DDD)
 * - CQRS (Command Query Responsibility Segregation)
 * - Event-Driven Architecture
 * - Hexagonal Architecture
 */
@SpringBootApplication
@EnableJpaAuditing              // JPA Auditing 활성화 (생성일시, 수정일시 자동 관리)
@EnableTransactionManagement    // 트랜잭션 관리 활성화
@EnableAsync                    // 비동기 처리 활성화
@EnableScheduling               // 스케줄러 활성화 (정산 배치 작업용)
@EnableCaching                  // 캐싱 활성화
@EnableKafka                    // Kafka 활성화 (이벤트 메시징)
public class Application {

    /**
     * 애플리케이션 진입점
     *
     * @param args 명령행 인자
     */
    public static void main(String[] args) {
        // Spring Boot 애플리케이션 시작
        SpringApplication application = new SpringApplication(Application.class);

        // 배너 설정 (선택사항)
        // application.setBannerMode(Banner.Mode.OFF);

        // 기본 프로파일 설정 (없는 경우)
        application.setDefaultProperties(java.util.Map.of(
                "spring.profiles.default", "local"
        ));

        // 애플리케이션 실행
        application.run(args);
    }
}