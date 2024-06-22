package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Handles the rest endpoint for publicly available information requiring no auth.
 */
@RestController
@RequestMapping("/v1/public")
@CrossOrigin(origins = "*")
public class PublicStockController {

    private final MemeStockService stockService;
    private final MemeStockRepository memeStockRepository;
    private static final Logger logger = Logger.getLogger(PublicStockController.class.getName());

    @Autowired
    public PublicStockController(MemeStockService stockService, MemeStockRepository memeStockRepository) {
        this.stockService = stockService;
        this.memeStockRepository = memeStockRepository;
    }

    /**
     * @param stockId The id of the meme stock to get the price of.
     * @return The price of the stock or 0 if the stock doesn't exist.
     */
    @GetMapping("/stock/price")
    public ResponseEntity<Integer> price(@RequestParam("stockId") int stockId) {
        if (stockId < 1) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(stockService.getPrice(stockId));
        } catch (StockNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stock/history")
    public ResponseEntity<StockHistoryV1> getStockHistory(
            @RequestParam("stockId") int stockId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        try {
            var start = OffsetDateTime.parse(startDate);
            var end = OffsetDateTime.parse(endDate);
            return ResponseEntity.ok(new StockHistoryV1(stockService.getHistory(stockId, start, end)));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stock/metadata")
    public ResponseEntity<StockMetadataV1> stockMetadata(@RequestParam("stockId") int stockId) {
        if (stockId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        var result = stockService.getMetadata(stockId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * @return a list of stock ids to be displayed on the front page.
     */
    @GetMapping("/frontpagestocks")
    public List<Integer> getFrontpageStocks() {
        return List.of(8, 9, 10, 11, 12, 13, 5, 6, 7, 14, 15, 16, 17, 18, 19);
    }

    /**
     * Gets the most recent articles.
     * @param num The maximum number of articles to return, starting from most recent.
     * @return A list of recent articles.
     */
    @GetMapping("/articles")
    public ResponseEntity<List<StockArticle>> getArticles(@RequestParam("num") int num) {
        return ResponseEntity.ok(memeStockRepository.getArticles(num));
    }

    @Autowired
    private UserService userService;

    /**
     * Begins the user verification process.
     * @param request
     * @return
     */
    @PostMapping("/createUser")
    public SseEmitter createUser(@RequestBody CreateUserRequest request) throws IOException {
        SseEmitter emitter = new SseEmitter();
        userService.beginVerifyingUser(request.userName(), request.email(), request.password(), emitter);
        return emitter;
    }

    private record CreateUserResponse(boolean success) {};
    private record CreateUserRequest(String email, String password, String userName) {};


//    @GetMapping("/articles/{articleId}/comments")
//    public ResponseEntity<List<Comment>> getArticleComments(@PathVariable Long articleId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
//        // TODO: Implement logic to query the database with pagination and return comments for the given articleId
//    }

    @GetMapping("/searchStocks")
    public ResponseEntity<List<StockSearchResultV1>> searchStocks(@RequestParam("searchString") String searchString) {
        return ResponseEntity.ok(stockService.searchStocks(searchString, 10));
    }
}
