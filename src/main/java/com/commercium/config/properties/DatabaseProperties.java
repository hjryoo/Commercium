package com.commercium.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.database")
public class DatabaseProperties {

    private DataSourceProperties write;
    private DataSourceProperties read;

    @Data
    public static class DataSourceProperties {
        private String url;
        private String username;
        private String password;
    }
}

