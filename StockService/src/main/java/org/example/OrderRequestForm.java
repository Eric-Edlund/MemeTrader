package org.example;

public record OrderRequestForm(int stockId, int userId, String operation, int pricePerShare, int numShares, int totalPrice) {
    public StockOrder order() {

        return switch (operation) {
            case "BUY" -> StockOrder.Buy;
            case "SELL" -> StockOrder.Sell;
            default -> throw new IllegalStateException("Unexpected value: " + operation);
        };
    }
}
