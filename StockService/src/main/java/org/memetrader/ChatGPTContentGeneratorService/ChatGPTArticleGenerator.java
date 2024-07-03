package org.memetrader.ChatGPTContentGeneratorService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.memetrader.common.MemeStockRepository;
import org.memetrader.WebServer.MemeStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.stream;

/**
 * Generates news articles about the stock market using ChatGPT and recent data.
 */
@Service
public class ChatGPTArticleGenerator {

    @Autowired
    MemeStockRepository memeStockRepository;

    @Autowired
    MemeStockService memeStockService;

    private static Logger logger = Logger.getLogger(ChatGPTDescriptionGenerator.class.getName());

    /**
     * Looks through the database for memes without descriptions, then uses
     * ChatGPT to generate descriptions for them.
     */
    @Scheduled(fixedRate = 3600000)
    public void generateDailyArticle() {
        final var lastArticleDate = memeStockRepository.lastPublishedStory();
        if (lastArticleDate != null && !lastArticleDate.isBefore(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS))) {
            System.out.println("We already published an article today.");
            return;
        }

        final var stockData = getRecentData();
        System.out.println(stockData);
        String textResponse = generateArticle(stockData);
        final var components = extractTitleAndBody(textResponse);

        System.out.println(components.get(0) + "\n" + components.get(1));

        final var image = generateImage(components.get(0), components.get(1));
        final var uuid = UUID.randomUUID();
        try {
            assert image != null;
            Files.write(Paths.get("static/" + uuid + ".png"), image, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        memeStockRepository.addArticle(components.get(0), components.get(1), "http://localhost:8080/" + uuid + ".png");
    }

    private byte[] generateImage(String articleTitle, String articleBody) {
        try {
            final String url = "https://api.openai.com/v1/images/generations";
            final var conn = (HttpURLConnection) new URL(url).openConnection();
            conn.addRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("Accept", "application/json");
            conn.addRequestProperty("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            final var encoder = JsonStringEncoder.getInstance();

            final var prompt = "Create an image to go with this article. It should match the style of any memes which are in the article. Give it a semi-serious tone." +
                    String.format("Article title: %s", new String(encoder.quoteAsString(articleTitle))) +
                    String.format("Article body: %s", new String(encoder.quoteAsString(articleBody.replaceAll("\n", " "))));
            final var truncatedPrompt = prompt.substring(0, Math.min(prompt.length(), 500)) + "...";

            final var request = "{" +
                    "\"model\": \"dall-e-3\"," +
                    "\"prompt\": \"" + truncatedPrompt + "\"," +
                    "\"response_format\": \"b64_json\"," +
                    "\"n\": 1," +
                    "\"size\": \"1792x1024\"" +
                    "}";

            conn.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();
            conn.connect();

            final var textResponse = new String(conn.getInputStream().readAllBytes());
            final String imageB64 = JsonPath.read(textResponse, "$.data[0].b64_json");
            final var bytes = Base64.getDecoder().decode(imageB64);
            conn.disconnect();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateArticle(String stockData) {
        try {
            final String url = "https://api.openai.com/v1/chat/completions";
            final var conn = (HttpURLConnection) new URL(url).openConnection();
            conn.addRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("Accept", "application/json");
            conn.addRequestProperty("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            final var request = "{" +
                    "\"model\": \"gpt-4\"," +
                    "\"messages\": [\n" +
                    "      {" +
                    "        \"role\": \"system\"," +
                    "        \"content\": \"Write a short news story about an event relevant to the meme stock market. By meme stock market, I mean a joke stock market I've created in which you can trade stocks in actual memes. Avoid referring to the market as the \\\"meme stock market\\\" or analysts as \\\"meme stock analysts\\\". Instead refer to them as just \\\"the market\\\" and \\\"analysts\\\" respectively. Format the response without markdown. Make the first line the article title, then the rest will be interpreted as the article. DO NOT put the article title in quotes, it should just be a line of text. Analyze the given stock data for inspiration. The prices are in cents.\"" +
                    "      }," +
                    "      {\n" +
                    "        \"role\": \"user\",\n" +
                    "        \"content\": \"" +
                    String.format("%s\"", stockData) +
                    "      }\n" +
                    "    ]" +
                    "}";
            conn.setRequestProperty("Content-Length", String.valueOf(request.getBytes(StandardCharsets.UTF_8).length));
            conn.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Article request response code " + responseCode + " " + conn.getResponseMessage());
            }

            final var textResponse = new String(conn.getInputStream().readAllBytes());
            conn.disconnect();
            return textResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRecentData() {
        final var result = new StringBuilder();
        result.append("stock_name,last_week_price,yesterday_price,today_price,");

        List<Integer> stockIds = memeStockRepository.getAllStocks();
        assert stockIds.size() < 200; // Tokens aren't free, implement selectivity logic below.
        final var now = OffsetDateTime.now();
        for (int id : stockIds) {
            final var name = memeStockRepository.getMetadata(id).title();
            final var lastWeekHistory = memeStockService.getHistory(id, now.minusDays(7), now.minusDays(6));
            final var yesterdayHistory = memeStockService.getHistory(id, now.minusDays(2), now.minusDays(1));
            final var todayHistory = memeStockService.getHistory(id, now.minusDays(1), now);

            final var lastWeek = lastWeekHistory.isEmpty() ? 0 : lastWeekHistory.get(lastWeekHistory.size() - 1).price();
            final var yesterday = yesterdayHistory.isEmpty() ? 0 : yesterdayHistory.get(yesterdayHistory.size() - 1).price();
            final var today = todayHistory.isEmpty() ? 0 : todayHistory.get(todayHistory.size() - 1).price();

            // TODO: Be selective about which stocks are interesting
            result.append(JsonStringEncoder.getInstance().quoteAsString(String.format("%s,%d,%d,%d", name, lastWeek, yesterday, today)));
        }

        return result.toString();
    }

    private record ChatGPTResponse(List<ChatGPTArticleGenerator.Choice> choices) {};
    private record Choice(ChatGPTArticleGenerator.Message message) {};
    private record Message(String role, String content) {};

    private List<String> extractTitleAndBody(String response) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            final var result = mapper.readValue(response, ChatGPTArticleGenerator.ChatGPTResponse.class);
            final var content = result.choices().get(0).message.content();

            final var articleTitle = content.substring(0, content.indexOf('\n'));
            final var articleBody = content.substring(content.indexOf('\n'));

            return List.of(articleTitle, articleBody);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to parse response from chat gpt");
            throw new RuntimeException(e);
        }
    }
}
