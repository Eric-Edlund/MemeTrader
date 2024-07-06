package com.memetrader.common;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String s) {
        super(s);
    }
}
