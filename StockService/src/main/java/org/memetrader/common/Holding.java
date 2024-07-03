package org.memetrader.common;

import org.springframework.lang.Nullable;

public record Holding(int stockId, long amtOwned, @Nullable Long sharePrice) {};

