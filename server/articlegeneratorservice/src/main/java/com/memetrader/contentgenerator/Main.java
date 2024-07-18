package com.memetrader.contentgenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Path;

@ComponentScan({ "com.memetrader.contentgenerator", "com.memetrader.common", "com.memetrader.config"})
@SpringBootApplication
@EnableScheduling
public class Main {
    private static final String _read_path = System.getenv("IMAGE_STORE_PATH");

    /**
     * Directory to store images.
     * The generated article images will be stored beneath this in ARTICLE_SUB_PATH
     */
    public static final String IMAGE_STORE_PATH = _read_path == null ? System.getProperty("user.dir") : _read_path;

    public static final String ARTICLE_SUB_PATH = "articles";

    public static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

    @Autowired
    ChatGPTDescriptionGenerator chatGPTDescriptionGenerator;

    @Autowired
    ChatGPTArticleGenerator articleGenerator;

    public static void main(String[] args) {
        System.out.println("IMAGE_STORE_PATH: " + Path.of(IMAGE_STORE_PATH).toAbsolutePath());
        if (IMAGE_STORE_PATH.isBlank()) {
            System.out.println("IMAGE_STORE_PATH is unspecified, using current directory.");
        }
        if (OPENAI_API_KEY == null) {
            System.out.println("OPENAI_API_KEY must be set.");
            System.exit(1);
        }
        SpringApplication.run(Main.class);
    }
}
