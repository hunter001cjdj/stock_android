package com.example.twstockanalyzer.data.analysis

import com.example.twstockanalyzer.domain.model.MetricItem
import com.example.twstockanalyzer.domain.model.RiskRewardQuadrant
import com.example.twstockanalyzer.domain.model.StockAnalysis
import com.example.twstockanalyzer.domain.model.StockRawData
import kotlin.math.roundToInt

class StockScoringEngine {
    fun analyze(stock: StockRawData): StockAnalysis {
        val rewardScore = (
            normalizePositive(stock.monthlyRevenueGrowth, 0.35) * 0.20 +
                normalizePositive(stock.epsGrowth, 0.35) * 0.20 +
                normalizePositive(stock.roe, 25.0) * 0.18 +
                normalizePositive(stock.grossMargin, 60.0) * 0.12 +
                normalizePositive(stock.volumeMomentum, 1.0) * 0.12 +
                normalizePositive(stock.trendStrength, 1.0) * 0.18
            ).times(100).roundToInt().coerceIn(0, 100)

        val riskScore = (
            normalizePositive(stock.volatility, 0.12) * 0.34 +
                normalizePositive(stock.drawdown, 0.25) * 0.24 +
                normalizePositive(stock.debtRatio, 0.70) * 0.18 +
                normalizePositive(stock.peRatio, 35.0) * 0.16 +
                normalizeNegative(stock.trendStrength, 1.0) * 0.08
            ).times(100).roundToInt().coerceIn(0, 100)

        val finalScore = (
            rewardScore * 0.58 +
                (100 - riskScore) * 0.22 +
                normalizePositive(stock.volumeMomentum, 1.0) * 100 * 0.10 +
                normalizePositive(stock.trendStrength, 1.0) * 100 * 0.10
            ).roundToInt().coerceIn(0, 100)

        val quadrant = when {
            riskScore >= 60 && rewardScore >= 60 -> RiskRewardQuadrant.HIGH_RISK_HIGH_REWARD
            riskScore < 60 && rewardScore >= 60 -> RiskRewardQuadrant.LOW_RISK_HIGH_REWARD
            riskScore >= 60 && rewardScore < 60 -> RiskRewardQuadrant.HIGH_RISK_LOW_REWARD
            else -> RiskRewardQuadrant.LOW_RISK_LOW_REWARD
        }

        val starRating = when {
            finalScore >= 82 -> 5
            finalScore >= 68 -> 4
            finalScore >= 54 -> 3
            finalScore >= 40 -> 2
            else -> 1
        }

        val recommendReasons = buildRecommendReasons(stock, rewardScore, riskScore)
        val avoidReasons = buildAvoidReasons(stock, riskScore)

        return StockAnalysis(
            stockId = stock.stockId,
            stockName = stock.stockName,
            priceLabel = "NT$ %.1f".format(stock.price),
            dailyChangeLabel = "%+.2f%%".format(stock.dailyChangePercent),
            starRating = starRating,
            finalScore = finalScore,
            riskScore = riskScore,
            rewardScore = rewardScore,
            quadrant = quadrant,
            recommendReasons = recommendReasons,
            avoidReasons = avoidReasons,
            summary = buildSummary(stock.stockName, quadrant, starRating),
            metrics = listOf(
                MetricItem("Revenue YoY", percent(stock.monthlyRevenueGrowth)),
                MetricItem("EPS Growth", percent(stock.epsGrowth)),
                MetricItem("ROE", percent(stock.roe / 100)),
                MetricItem("Gross Margin", percent(stock.grossMargin / 100)),
                MetricItem("Debt Ratio", percent(stock.debtRatio)),
                MetricItem("PE Ratio", "%.1f".format(stock.peRatio))
            )
        )
    }

    private fun buildRecommendReasons(
        stock: StockRawData,
        rewardScore: Int,
        riskScore: Int
    ): List<String> {
        val reasons = mutableListOf<String>()
        if (stock.monthlyRevenueGrowth > 0.15) reasons += "Revenue growth is strong and improving."
        if (stock.epsGrowth > 0.12) reasons += "EPS momentum remains healthy."
        if (stock.roe > 15.0) reasons += "ROE shows solid capital efficiency."
        if (stock.volumeMomentum > 0.45) reasons += "Volume trend suggests higher market attention."
        if (stock.trendStrength > 0.55) reasons += "Price trend is constructive."
        if (rewardScore >= 70 && riskScore < 55) reasons += "Risk/reward profile is attractive."
        return reasons.take(4).ifEmpty { listOf("Overall profile is neutral and worth monitoring.") }
    }

    private fun buildAvoidReasons(stock: StockRawData, riskScore: Int): List<String> {
        val reasons = mutableListOf<String>()
        if (stock.volatility > 0.09) reasons += "Volatility is elevated."
        if (stock.drawdown > 0.18) reasons += "Recent drawdown is still meaningful."
        if (stock.debtRatio > 0.55) reasons += "Debt ratio is on the high side."
        if (stock.peRatio > 28) reasons += "Valuation already prices in optimism."
        if (stock.monthlyRevenueGrowth < 0.05) reasons += "Revenue growth is limited."
        if (riskScore >= 70) reasons += "Overall risk level is high."
        return reasons.take(4).ifEmpty { listOf("No major red flag, but position sizing still matters.") }
    }

    private fun buildSummary(
        stockName: String,
        quadrant: RiskRewardQuadrant,
        starRating: Int
    ): String = when (quadrant) {
        RiskRewardQuadrant.HIGH_RISK_HIGH_REWARD ->
            "$stockName is a high-volatility growth profile with upside and higher uncertainty. Current rating: $starRating stars."
        RiskRewardQuadrant.LOW_RISK_HIGH_REWARD ->
            "$stockName offers one of the strongest balanced risk/reward profiles right now. Current rating: $starRating stars."
        RiskRewardQuadrant.HIGH_RISK_LOW_REWARD ->
            "$stockName currently carries more risk than expected reward. Current rating: $starRating stars."
        RiskRewardQuadrant.LOW_RISK_LOW_REWARD ->
            "$stockName looks relatively stable, but growth catalysts are limited. Current rating: $starRating stars."
        RiskRewardQuadrant.ALL -> "$stockName"
    }

    private fun percent(value: Double): String = "%+.1f%%".format(value * 100)

    private fun normalizePositive(value: Double, upperBound: Double): Double {
        return (value / upperBound).coerceIn(0.0, 1.0)
    }

    private fun normalizeNegative(value: Double, upperBound: Double): Double {
        return 1.0 - normalizePositive(value, upperBound)
    }
}
