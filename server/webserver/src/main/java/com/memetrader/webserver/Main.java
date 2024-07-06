package com.memetrader.webserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({ "com.memetrader.webserver", "com.memetrader.common", "com.memetrader.config" })
@SpringBootApplication
public class Main {

    @Autowired
    PublicStockController restController;

    @Autowired
    LoginController loginController;

    @Autowired
    UserController userMemeStockController;

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }
}
