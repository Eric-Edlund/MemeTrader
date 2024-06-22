package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    @Autowired
    PublicStockController restController;

    @Autowired
    LoginController loginController;

    @Autowired
    UserStockController userMemeStockController;

//    @Autowired
//    ImgFlipMemeFetcher imgFlipMemeFetcher;

    @Autowired
    ChatGPTDescriptionGenerator chatGPTDescriptionGenerator;
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }
}
