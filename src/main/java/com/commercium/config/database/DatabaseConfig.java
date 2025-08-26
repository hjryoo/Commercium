package com.commercium.config.database;

import com.commercium.config.properties.DatabaseProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(DatabaseProperties.class)
@RequiredArgsConstructor
public class DatabaseConfig {

    private final DatabaseProperties databaseProperties;

    /**
     * 쓰기 전용 데이터소스 (Master DB)
     */
    @Bean
    @Primary
    public DataSource writeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseProperties.getWrite().getUrl());
        config.setUsername(databaseProperties.getWrite().getUsername());
        config.setPassword(databaseProperties.getWrite().getPassword());
        config.setDriverClassName("org.postgresql.Driver");

        // 커넥션 풀 최적화
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        // 성능 최적화
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new LazyConnectionDataSourceProxy(new HikariDataSource(config));
    }

    /**
     * 읽기 전용 데이터소스 (Replica DB) - CQRS 패턴
     */
    @Bean
    @Profile("!test")
    public DataSource readDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseProperties.getRead().getUrl());
        config.setUsername(databaseProperties.getRead().getUsername());
        config.setPassword(databaseProperties.getRead().getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        config.setReadOnly(true);

        // 읽기 전용이므로 더 많은 커넥션 허용
        config.setMaximumPoolSize(30);
        config.setMinimumIdle(10);

        return new LazyConnectionDataSourceProxy(new HikariDataSource(config));
    }
}
