package org.example;

import org.springframework.lang.NonNull;

public record OrderRequestForm(int stockId, int userId, @NonNull String operation, long numShares, long totalPrice) {
    public StockOrder order() {

        return switch (operation) {
            case "BUY" -> StockOrder.Buy;
            case "SELL" -> StockOrder.Sell;
            default -> throw new IllegalStateException("Unexpected value: " + operation);
        };
    }
}
