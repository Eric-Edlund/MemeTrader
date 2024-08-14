package com.memetrader.contentgenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memetrader.common.MemeStockRepository;
import com .memetrader.common.StockMetadataV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Generates missing descriptions for stocks using AI.
 */
@Service
public class ChatGPTDescriptionGenerator {

    private static final Logger logger = Logger.getLogger(ChatGPTDescriptionGenerator.class.getName());

    @Autowired
    MemeStockRepository memeStockRepository;

    /**
     * Looks through the database for memes without descriptions, then uses
     * ChatGPT to generate descriptions for them.
     */
    @Scheduled(fixedRate = 3600000)
    public void generateMissingDescriptions() {
        List<Integer> ids = memeStockRepository.getMissingDescriptions();
        for (var id : ids) {
            final String url = "https://api.openai.com/v1/chat/completions";
            final StockMetadataV1 meme = memeStockRepository.getMetadata(id);
            assert meme != null;

            try {
                final var conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.addRequestProperty("Content-Type", "application/json");
                conn.addRequestProperty("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.getOutputStream().write(("{" +
                        "\"model\": \"gpt-4\"," +
                        "\"messages\": [\n" +
                        "      {\n" +
                        "        \"role\": \"system\",\n" +
                        "        \"content\": \"Write a historical summary of the given meme. This will go in the about section of a website where people buy stocks in memes, so don't include information that is the same for all memes. Focus only on this meme. Aim for 200 words. Do not comment directly on if the user should buy the meme or not.\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"role\": \"user\",\n" +
                        "        \"content\": \"" + meme.title() + "\"\n" +
                        "      }\n" +
                        "    ]" +
                        "}").getBytes(StandardCharsets.UTF_8));
                conn.getOutputStream().flush();
                conn.connect();
                final var response = new String(conn.getInputStream().readAllBytes());
                conn.disconnect();
                processResponse(id, response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record ChatGPTResponse(List<Choice> choices) {};
    private record Choice(Message message) {};
    private record Message(String role, String content) {};

    private void processResponse(int stockId, String response) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            final var result = mapper.readValue(response, ChatGPTResponse.class);
            final var content = result.choices().get(0).message.content();
            memeStockRepository.addDescription(stockId, content);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to parse response from chat gpt");
            throw new RuntimeException(e);
        }
    }
}
