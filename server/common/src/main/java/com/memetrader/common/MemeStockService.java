package com.memetrader.common;

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
     */
    public long getPrice(int stockId) {
        return price(memeStockRepository.getTotalOwnedShares(stockId));
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
     * @param totalPrice The price of the order.
     * @param dryRun If true, don't commit the transaction, just the error if there is one.
     * @return The order number.
     * @throws StockOrderException If the order is invalid.
     */
    public int placeOrder(int userId, int stockId, StockOrder stockOrder, long numShares, long totalPrice, boolean dryRun) throws StockOrderException{
        //TODO: Do we need this check if the database checks ledger updates for us?
        if (stockOrder == StockOrder.Sell && !isSellOrderAllowed(userId, stockId, numShares)) {
            throw new StockOrderException(StockOrderException.Problem.InsufficientHoldings);
        }

        long currentOwnedShares = memeStockRepository.getTotalOwnedShares(stockId);
        long correctTotalPrice = transactionPrice(currentOwnedShares, currentOwnedShares + numShares);
        if (correctTotalPrice != totalPrice) {
            System.out.println("Expected total price " + (correctTotalPrice) + " got price " + totalPrice);
            throw new StockOrderException(StockOrderException.Problem.InvalidOrder, correctTotalPrice);
        }

        return memeStockRepository.writeToLedger(userId, stockId, stockOrder, numShares, totalPrice, dryRun);
    }

    /**
     * The cost of changing the number of shares owned from a to b.
     * @param sharesA Number of shares owned before operation.
     * @param sharesB Number of shares owned after operation.
     * @return Always positive
     */
   private long transactionPrice(long sharesA, long sharesB) {
        if (sharesA > sharesB) {
            long tmp = sharesA;
            sharesA = sharesB;
            sharesB = tmp;
        }
        return (PRICE_COEF * (sharesB - sharesA) * (sharesB - sharesA + 1) / 2)
                - 6 * (sharesB - sharesA);

    }

    private long price(long numShares) {
        return numShares * PRICE_COEF + 1;
    }

    public boolean isSellOrderAllowed(int userId, int stockId, long numShares) {
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

    public Map<OffsetDateTime, BalanceHoldingsPair> getAccountHistory(OffsetDateTime start, OffsetDateTime end, int userId) {
        return memeStockRepository.getAccountHistory(start, end, userId).entrySet().stream()
                .map(entry -> {
                    final var balance = entry.getValue().balance();
                    final List<MemeStockRepository.RawHolding> rawHoldings = entry.getValue().holdings();
                    return Map.entry(entry.getKey(), new BalanceHoldingsPair(
                            balance,
                            rawHoldings.stream()
                                    .map(rawHolding -> new Holding(
                                            rawHolding.stockId(),
                                            rawHolding.amtOwned(),
                                            rawHolding.totalOwned() * PRICE_COEF + 1
                                    ))
                                    .toList()
                    ));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    public long getTransactionValue(int stockId, String operation, long numShares) {
        final var currentOwned = memeStockRepository.getTotalOwnedShares(stockId);
        final var finalOwned = currentOwned + switch (operation) {
            case "BUY" -> numShares;
            case "SELL" -> -numShares;
            default -> throw new IllegalStateException("Unexpected value: " + operation);
        };
        return transactionPrice(currentOwned, finalOwned);
    }
}
