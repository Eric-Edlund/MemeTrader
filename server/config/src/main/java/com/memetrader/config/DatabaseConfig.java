package com.memetrader.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/MemeStockExchange";
    private static final String DB_USER = System.getenv("DB_USERNAME");
    private static final String DB_PASS = System.getenv("DB_PASSWORD");

    public DatabaseConfig() {
        if (DB_USER == null) {
            System.out.println("Must specify DB_USERNAME");
            System.exit(1);
        }
        if (DB_PASS == null) {
            System.out.println("Must specify DB_PASSWORD");
            System.exit(1);
        }
    }

    @Bean
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASS);
        config.setMaximumPoolSize(10);

        try {
            return new HikariDataSource(config);
        } catch (RuntimeException e) {
            System.out.println("Failed to connect to database.");
            System.exit(1);
            return null;
        }
    }
}