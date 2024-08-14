package com.memetrader.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Value("${db.url}")
    public String DB_URL;
    @Value("${db.username}")
    public String DB_USER;
    @Value("${db.password}")
    public String DB_PASS;

    Logger logger = Logger.getLogger(DatabaseConfig.class.getName());

    @PostConstruct
    public void init() throws IOException {

        logger.addHandler(new FileHandler("startup.log"));

        if (DB_URL == null) {
            System.out.println("Must specify db.url");
            logger.log(Level.SEVERE, "Must specify db.url");
            throw new RuntimeException("db url not specified.");
        }

        if (!DB_URL.contains("sqlite")) {
            if (DB_USER == null) {
                System.out.println("Must specify db.username");
                logger.log(Level.SEVERE, "Must specify db.username");
                throw new RuntimeException("db username not specified.");
            }
            if (DB_PASS == null) {
                System.out.println("Must specify db.password");
                logger.log(Level.SEVERE, "Must specify db.password");
                throw new RuntimeException("db password not specified.");
            }
        }
    }

    @Bean
    public HikariDataSource dataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        if (DB_USER != null)
            config.setUsername(DB_USER);
        if (DB_PASS != null)
            config.setPassword(DB_PASS);
        config.setMaximumPoolSize(10);

        return new HikariDataSource(config);
    }
}
