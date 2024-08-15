package com.memetrader.webserver;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({ "com.memetrader.webserver", "com.memetrader.common" })
@SpringBootApplication
public class Main {

    @Autowired
    PublicStockController restController;

    @Autowired
    LoginController loginController;

    @Autowired
    UserController userMemeStockController;

    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getGlobal();
        logger.addHandler(new FileHandler("main.log"));
        SpringApplication.run(Main.class);
    }
}
