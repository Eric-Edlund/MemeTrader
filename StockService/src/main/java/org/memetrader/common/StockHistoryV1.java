package org.memetrader.common;

import org.memetrader.WebServer.PricePoint;

import java.util.List;

public record StockHistoryV1(List<PricePoint> points) {}
