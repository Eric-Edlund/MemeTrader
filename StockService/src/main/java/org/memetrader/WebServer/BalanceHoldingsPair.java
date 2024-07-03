package org.memetrader.WebServer;

import org.memetrader.common.Holding;

import java.util.List;

public record BalanceHoldingsPair(long balance, List<Holding> holdings) {};
