package org.example;

import org.springframework.lang.Nullable;

public record Holding(int stockId, int amtOwned, @Nullable Integer sharePrice) {};
