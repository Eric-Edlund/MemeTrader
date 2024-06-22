package org.example;

import java.time.LocalDateTime;
import java.util.List;

public record StockHistoryV1(List<PricePoint> points) {}
