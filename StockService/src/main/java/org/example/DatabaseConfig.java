package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/MemeStockExchange";
    private static final String DB_USER = "memestockserver";
    private static final String DB_PASS = "1234"; //System.getenv("DB_PASSWORD");
    @Bean
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASS);
        config.setMaximumPoolSize(10);

        return new HikariDataSource(config);
    }
}