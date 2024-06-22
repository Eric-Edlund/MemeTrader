package org.example;

public class PlaceOrderFailed extends Exception {
    public PlaceOrderFailed(String reason) {
        super(reason);
    }
}
