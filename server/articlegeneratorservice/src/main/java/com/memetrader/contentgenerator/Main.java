package com.memetrader.contentgenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan({ "com.memetrader.contentgenerator", "com.memetrader.common" })
@SpringBootApplication
@EnableScheduling
public class Main {

    @Autowired
    ChatGPTDescriptionGenerator chatGPTDescriptionGenerator;

    @Autowired
    ChatGPTArticleGenerator articleGenerator;

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }
}
