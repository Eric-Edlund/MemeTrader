package org.example;

import java.time.OffsetDateTime;

public record PricePoint(OffsetDateTime time, long price) {
}
