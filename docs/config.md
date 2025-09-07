```markdown
com.commercium.config/
├── database/
│   ├── DatabaseConfig.java        // DB 연결 설정 (Write/Read 분리)
│   ├── RedisConfig.java          // Redis 캐시/락 설정
│   └── JpaConfig.java            // JPA 감사/설정
├── messaging/
│   ├── KafkaConfig.java          // Kafka 프로듀서/컨슈머
│   └── RabbitMQConfig.java       // RabbitMQ 설정
├── security/
│   ├── SecurityConfig.java       // Spring Security + JWT
│   ├── JwtConfig.java           // JWT 토큰 설정
│   └── CorsConfig.java          // CORS 설정
├── api/
│   ├── OpenApiConfig.java       // Swagger/OpenAPI 설정
│   └── WebConfig.java           // Web MVC 설정
├── async/
│   ├── AsyncConfig.java         // 비동기 처리 설정
│   └── SchedulerConfig.java     // 배치/스케줄러 설정
└── properties/
    ├── DatabaseProperties.java  // DB 프로퍼티
    ├── RedisProperties.java     // Redis 프로퍼티
    └── JwtProperties.java       // JWT 프로퍼티
```
