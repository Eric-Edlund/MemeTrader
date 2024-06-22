package org.example;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String s) {
        super(s);
    }
}
