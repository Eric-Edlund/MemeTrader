package org.memetrader.WebServer;

import java.time.OffsetDateTime;

public record PricePoint(OffsetDateTime time, long price) {
}
