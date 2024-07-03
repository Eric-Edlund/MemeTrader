package org.memetrader.WebServer;

import org.memetrader.ChatGPTContentGeneratorService.ChatGPTDescriptionGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({ "org.memetrader.WebServer", "org.memetrader.common", "org.memetrader.config" })
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
