package org.example;

import java.time.OffsetDateTime;

/**
 * A news story displayed on the site.
 */
public record StockArticle(int id, String title, OffsetDateTime published, String body, String imageUrl) {
}
