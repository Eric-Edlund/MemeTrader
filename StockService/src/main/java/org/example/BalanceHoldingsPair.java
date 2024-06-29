package org.example;

import java.util.List;

public record BalanceHoldingsPair(long balance, List<Holding> holdings) {};
