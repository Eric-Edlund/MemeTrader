package org.memetrader.WebServer;

public class StockOrderException extends Exception {
    public StockOrderException(Problem problem) {
        super(problem.name());
        this.problem = problem;
    }
    public StockOrderException(Problem problem, long correctTotalPrice) {
        super(problem.name());
        this.problem = problem;
        this.correctTotalPrice = correctTotalPrice;
    }

    public final Problem problem;
    public Long correctTotalPrice = null;


    public enum Problem {
        /**
         * The user does not have a balance sufficient to buy the expected amount of stocks.
         */
        InsufficientFunds,
        /**
         * The user does not own enough of the stock to sell the expected amount.
         */
        InsufficientHoldings,
        /**
         * The order itself is inconsistent.
         */
        InvalidOrder,
    }
}
