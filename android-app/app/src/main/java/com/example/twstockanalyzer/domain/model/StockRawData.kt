package com.example.twstockanalyzer.domain.model

data class StockRawData(
    val stockId: String,
    val stockName: String,
    val price: Double,
    val dailyChangePercent: Double,
    val monthlyRevenueGrowth: Double,
    val epsGrowth: Double,
    val roe: Double,
    val grossMargin: Double,
    val debtRatio: Double,
    val peRatio: Double,
    val volatility: Double,
    val drawdown: Double,
    val volumeMomentum: Double,
    val trendStrength: Double
)
