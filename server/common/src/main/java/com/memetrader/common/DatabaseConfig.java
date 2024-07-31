package com.memetrader.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
public class DatabaseConfig {

    @Value("${db.url}")
    public String DB_URL;
    @Value("${db.username}")
    public String DB_USER;
    @Value("${db.password}")
    public String DB_PASS;

    @PostConstruct
    public void init() {
        if (DB_USER == null) {
            System.out.println("Must specify db.username");
            throw new RuntimeException("db username not specified.");
        }
        if (DB_PASS == null) {
            System.out.println("Must specify db.password");
            throw new RuntimeException("db password not specified.");
        }
        if (DB_URL == null) {
            System.out.println("Must specify db.url");
            throw new RuntimeException("db url not specified.");
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
            throw new IllegalStateException("Failed to connect to database.");
        }
    }
}
