package com.memetrader.common;

import java.util.List;

public record BalanceHoldingsPair(long balance, List<Holding> holdings) {};
