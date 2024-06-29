package org.example;

import org.springframework.lang.Nullable;

public record Holding(int stockId, long amtOwned, @Nullable Long sharePrice) {};

