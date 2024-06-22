package org.example;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class MemeStockService {

    Logger logger = Logger.getLogger(MemeStockRepository.class.getName());

    @Autowired
    public MemeStockService(MemeStockRepository memeStockRepository) {
        this.memeStockRepository = memeStockRepository;
    }
    private final MemeStockRepository memeStockRepository;

    private static final int PRICE_COEF = 7;

    /**
     * Returns the price of a stock.
     *
     * @param stockId The id of the stock.
     * @return The price of the stock.
     * @throws StockNotFoundException If the stock does not exist.
     */    public int getPrice(int stockId) {
        return memeStockRepository.getTotalOwnedShares(stockId) * PRICE_COEF + 1;
    }

    /**
     * Returns the history of a stock.
     *
     * @param stockId The id of the stock.
     * @param startDate The start date of the history.
     * @param endDate The end date of the history.
     * @return The history of the stock.
     */
    public List<PricePoint> getHistory(int stockId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return memeStockRepository.getStockHistory(stockId, startDate, endDate).stream().map(pricePoint -> new PricePoint(pricePoint.time(), pricePoint.price() * PRICE_COEF + 1)).collect(Collectors.toList());
    }

    /**
     * Returns the metadata of a stock.
     *
     * @param stockId The id of the stock.
     * @return The metadata of the stock.
     */
    @Nullable
    public StockMetadataV1 getMetadata(int stockId) {
        return memeStockRepository.getMetadata(stockId);
    }


    /**
     * Places an order for a stock.
     *
     * @param userId The id of the user.
     * @param stockId The id of the stock.
     * @param stockOrder The type of order (buy or sell).
     * @param numShares The number of shares to buy or sell.
     * @param pricePerShare The price per share.
     * @return The order number.
     * @throws PlaceOrderFailed If the order is invalid.
     */
    public int placeOrder(int userId, int stockId, StockOrder stockOrder, int numShares, int pricePerShare) throws PlaceOrderFailed {
        // TODO: Check funding
        // TODO: Update acct balance
        if (stockOrder == StockOrder.Sell && !isSellOrderAllowed(userId, stockId, numShares)) {
            throw new PlaceOrderFailed("User does not have enough shares to sell");
        }
        return memeStockRepository.writeToLedger(userId, stockId, stockOrder, numShares, pricePerShare);
    }

    public boolean isSellOrderAllowed(int userId, int stockId, int numShares) {
        try {
            return memeStockRepository.getTotalOwnedSharesByUser(userId, stockId) >= numShares;
        } catch (StockNotFoundException e) {
            logger.log(Level.INFO, "Stock not found for id " + stockId);
            return false;
        }
    }

    /**
     * Searches for stocks by a search string.
     *
     * @param searchString The search string.
     * @param numResults The maximum number of results.
     * @return The search results.
     */
    public List<StockSearchResultV1> searchStocks(String searchString, int numResults) {
        // TODO: Cache this
        final var options = memeStockRepository.getAllMetadata();
        final Map<String, List<StockSearchResultV1>> map = new HashMap<>();

        for (var option : options) {
            map.putIfAbsent(option.title(), new ArrayList<>());
            map.get(option.title()).add(option);
        }

        return FuzzySearch.extractSorted(searchString, options.stream().map(StockSearchResultV1::title).collect(Collectors.toSet()))
                .stream()
                .map(title -> map.get(title.getString()))
                .flatMap(Collection::stream)
                .limit(numResults)
                .toList();
    }



}
