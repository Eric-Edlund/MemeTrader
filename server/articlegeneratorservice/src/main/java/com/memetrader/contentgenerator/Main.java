package com.memetrader.contentgenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({ "com.memetrader.contentgenerator", "com.memetrader.common", "com.memetrader.config"})
@SpringBootApplication
public class Main {

    @Autowired
    ChatGPTDescriptionGenerator chatGPTDescriptionGenerator;

    @Autowired
    ChatGPTArticleGenerator articleGenerator;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Main.class);
    }
}
