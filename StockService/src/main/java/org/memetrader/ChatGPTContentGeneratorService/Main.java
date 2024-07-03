package org.memetrader.ChatGPTContentGeneratorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    @Autowired
    ChatGPTDescriptionGenerator chatGPTDescriptionGenerator;

    @Autowired
    ChatGPTArticleGenerator articleGenerator;
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }
}
