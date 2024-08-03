package com.memetrader.contentgenerator;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class ContentGeneratorConfig {
    /**
     * Directory to store images.
     * The generated article images will be stored beneath this in ARTICLE_SUB_PATH
     */
    @Value("${IMAGE_STORE_PATH}")
    public String IMAGE_STORE_PATH;

    public String ARTICLE_SUB_PATH = "articles";

    @Value("${OPENAI_API_KEY}")
    public String OPENAI_API_KEY;

    @PostConstruct
    public void init() {
        if (IMAGE_STORE_PATH == null) {
            System.out.println("ERROR: IMAGE_STORE_PATH is unspecified.");
            System.exit(1);
        }
        System.out.println("IMAGE_STORE_PATH: " + Path.of(IMAGE_STORE_PATH).toAbsolutePath());

        if (OPENAI_API_KEY == null) {
            System.out.println("ERROR: OPENAI_API_KEY is unspecified.");
            System.exit(1);
        }
    }
}
