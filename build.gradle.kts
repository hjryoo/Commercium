plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.10"
    kotlin("plugin.jpa") version "1.9.10"
    java
}

group = "com.commercium"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

extra["springDocVersion"] = "2.2.0"
extra["testcontainersVersion"] = "1.19.1"
extra["redissonVersion"] = "3.24.3"
extra["jjwtVersion"] = "0.12.3"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Data Access
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    runtimeOnly("org.postgresql:postgresql:42.7.0")
    implementation("com.h2database:h2") // 테스트용

    // Connection Pool
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Redis & Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.redisson:redisson-spring-boot-starter:${property("redissonVersion")}")

    // Messaging
    implementation("org.springframework.boot:spring-boot-starter-amqp") // RabbitMQ
    implementation("org.springframework.kafka:spring-kafka")

    // Security & JWT
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwtVersion")}")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JSON Processing
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocVersion")}")

    // Async & Scheduling
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    // Configuration Properties
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Development Tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Utility
    implementation("org.apache.commons:commons-lang3:3.13.0")
    implementation("org.apache.commons:commons-collections4:4.4")

    // Excel Export (정산 리포트용)
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // Testcontainers
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:redis")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("org.testcontainers:kafka")

    // MockWebServer for external API testing
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    // Test Fixtures
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // 테스트 실행 시 JVM 옵션
    jvmArgs = listOf(
        "-XX:+EnableDynamicAgentLoading",
        "-Xmx2g",
        "-XX:MaxMetaspaceSize=512m"
    )

    // 테스트 병렬 실행
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")

    // Testcontainers 최적화
    systemProperty("testcontainers.reuse.enable", "true")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

// Docker support
tasks.register<Exec>("dockerBuild") {
    group = "docker"
    description = "Build Docker image"
    commandLine("docker", "build", "-t", "commercium:${version}", ".")
}

tasks.register<Exec>("dockerRun") {
    group = "docker"
    description = "Run Docker container"
    dependsOn("dockerBuild")
    commandLine("docker", "run", "-p", "8080:8080", "commercium:${version}")
}

// JAR 빌드 설정
tasks.bootJar {
    archiveFileName.set("commercium-${version}.jar")
    launchScript()
}

// 개발용 프로파일 실행 태스크
tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("bootRunDev") {
    group = "application"
    description = "Run the application with dev profile"
    args = listOf("--spring.profiles.active=dev")
}

// build.gradle.kts
springBoot {
    mainClass.set("com.commercium.Application")
}


// 테스트 커버리지 (선택사항)
// apply(plugin = "jacoco")

// configurations {
//     jacoco {
//         toolVersion = "0.8.10"
//     }
// }

// Gradle Wrapper
tasks.wrapper {
    gradleVersion = "8.4"
    distributionType = Wrapper.DistributionType.BIN
}
