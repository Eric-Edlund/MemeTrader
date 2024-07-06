package com.memetrader.common;

import com.memetrader.common.Holding;

import java.util.List;

public record BalanceHoldingsPair(long balance, List<Holding> holdings) {};
