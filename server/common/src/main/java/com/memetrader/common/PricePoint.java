package com.memetrader.common;

import java.time.OffsetDateTime;

public record PricePoint(OffsetDateTime time, long price) {
}
