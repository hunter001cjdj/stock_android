package com.example.twstockanalyzer.data.repository

import com.example.twstockanalyzer.data.analysis.StockScoringEngine
import com.example.twstockanalyzer.domain.model.StockAnalysis
import com.example.twstockanalyzer.domain.model.StockRawData

class FakeStockRepository(
    private val engine: StockScoringEngine = StockScoringEngine()
) : StockRepository {

    override fun getStocks(): List<StockAnalysis> {
        return sampleStocks
            .map(engine::analyze)
            .sortedByDescending { it.finalScore }
    }

    private val sampleStocks = listOf(
        StockRawData(
            stockId = "2330",
            stockName = "TSMC",
            price = 968.0,
            dailyChangePercent = 1.82,
            monthlyRevenueGrowth = 0.23,
            epsGrowth = 0.18,
            roe = 28.0,
            grossMargin = 0.54,
            debtRatio = 0.23,
            peRatio = 24.2,
            volatility = 0.05,
            drawdown = 0.08,
            volumeMomentum = 0.58,
            trendStrength = 0.74
        ),
        StockRawData(
            stockId = "2454",
            stockName = "MediaTek",
            price = 1315.0,
            dailyChangePercent = 2.14,
            monthlyRevenueGrowth = 0.19,
            epsGrowth = 0.16,
            roe = 24.0,
            grossMargin = 0.49,
            debtRatio = 0.19,
            peRatio = 21.5,
            volatility = 0.06,
            drawdown = 0.11,
            volumeMomentum = 0.52,
            trendStrength = 0.69
        ),
        StockRawData(
            stockId = "3661",
            stockName = "Alchip-KY",
            price = 2645.0,
            dailyChangePercent = 4.11,
            monthlyRevenueGrowth = 0.31,
            epsGrowth = 0.22,
            roe = 17.0,
            grossMargin = 0.61,
            debtRatio = 0.28,
            peRatio = 32.0,
            volatility = 0.11,
            drawdown = 0.19,
            volumeMomentum = 0.78,
            trendStrength = 0.83
        ),
        StockRawData(
            stockId = "2303",
            stockName = "UMC",
            price = 55.7,
            dailyChangePercent = -0.61,
            monthlyRevenueGrowth = 0.04,
            epsGrowth = 0.02,
            roe = 14.0,
            grossMargin = 0.34,
            debtRatio = 0.21,
            peRatio = 14.4,
            volatility = 0.04,
            drawdown = 0.06,
            volumeMomentum = 0.18,
            trendStrength = 0.29
        ),
        StockRawData(
            stockId = "1301",
            stockName = "Formosa Plastics",
            price = 61.2,
            dailyChangePercent = -0.48,
            monthlyRevenueGrowth = -0.02,
            epsGrowth = -0.06,
            roe = 7.0,
            grossMargin = 0.12,
            debtRatio = 0.37,
            peRatio = 18.8,
            volatility = 0.03,
            drawdown = 0.09,
            volumeMomentum = 0.09,
            trendStrength = 0.16
        ),
        StockRawData(
            stockId = "2382",
            stockName = "Quanta",
            price = 284.0,
            dailyChangePercent = 1.29,
            monthlyRevenueGrowth = 0.26,
            epsGrowth = 0.21,
            roe = 19.0,
            grossMargin = 0.22,
            debtRatio = 0.31,
            peRatio = 18.5,
            volatility = 0.07,
            drawdown = 0.10,
            volumeMomentum = 0.63,
            trendStrength = 0.72
        )
    )
}
