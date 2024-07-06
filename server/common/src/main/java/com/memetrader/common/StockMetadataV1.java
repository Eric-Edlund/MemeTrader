package com.memetrader.common;

import org.springframework.lang.NonNull;

/**
 * Describes a stock.
 * @param title
 * @param description
 * @param createdBy
 * @param symbol
 */
public record StockMetadataV1(@NonNull String title, String description, int createdBy, @NonNull String symbol, String imageUrl) {}
