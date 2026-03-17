package com.example.twstockanalyzer.data.analysis

import com.example.twstockanalyzer.domain.model.MetricItem
import com.example.twstockanalyzer.domain.model.RiskRewardQuadrant
import com.example.twstockanalyzer.domain.model.StockAnalysis
import com.example.twstockanalyzer.domain.model.StockRawData
import com.example.twstockanalyzer.domain.model.StockSector
import kotlin.math.roundToInt

class StockScoringEngine {
    fun analyze(stock: StockRawData): StockAnalysis {
        val rewardScore = (
            normalizePositive(stock.monthlyRevenueGrowth, 0.35) * 0.20 +
                normalizePositive(stock.epsGrowth, 0.35) * 0.20 +
                normalizePositive(stock.roe, 25.0) * 0.18 +
                normalizePositive(stock.grossMargin, 0.60) * 0.12 +
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

        return StockAnalysis(
            stockId = stock.stockId,
            stockName = stock.stockName,
            sector = StockSector.OTHER,
            priceLabel = "NT$ %.1f".format(stock.price),
            dailyChangeLabel = "%+.2f%%".format(stock.dailyChangePercent),
            starRating = starRating,
            finalScore = finalScore,
            riskScore = riskScore,
            rewardScore = rewardScore,
            quadrant = quadrant,
            recommendReasons = buildRecommendReasons(stock, rewardScore, riskScore),
            avoidReasons = buildAvoidReasons(stock, riskScore),
            summary = buildSummary(stock.stockName, quadrant, starRating),
            metrics = listOf(
                MetricItem("月營收成長", percent(stock.monthlyRevenueGrowth)),
                MetricItem("EPS 成長", percent(stock.epsGrowth)),
                MetricItem("ROE", percent(stock.roe / 100)),
                MetricItem("毛利率", percent(stock.grossMargin)),
                MetricItem("負債比", percent(stock.debtRatio)),
                MetricItem("本益比", "%.1f".format(stock.peRatio))
            )
        )
    }

    private fun buildRecommendReasons(
        stock: StockRawData,
        rewardScore: Int,
        riskScore: Int
    ): List<String> {
        val reasons = mutableListOf<String>()
        if (stock.monthlyRevenueGrowth > 0.15) reasons += "月營收成長動能明顯，基本面延續性不錯。"
        if (stock.epsGrowth > 0.12) reasons += "EPS 成長維持健康，獲利擴張不是只靠題材。"
        if (stock.roe > 15.0) reasons += "ROE 高於中位水準，資本使用效率具有優勢。"
        if (stock.volumeMomentum > 0.45) reasons += "量能有跟上，短線資金願意進場。"
        if (stock.trendStrength > 0.55) reasons += "趨勢強度偏正向，技術面沒有明顯轉弱。"
        if (rewardScore >= 70 && riskScore < 55) reasons += "報酬潛力與風險結構相對平衡。"
        return reasons.take(4).ifEmpty { listOf("整體條件穩定，但還需要更多即時資料確認。") }
    }

    private fun buildAvoidReasons(stock: StockRawData, riskScore: Int): List<String> {
        val reasons = mutableListOf<String>()
        if (stock.volatility > 0.09) reasons += "波動偏大，進出節奏需要更嚴格控管。"
        if (stock.drawdown > 0.18) reasons += "近期回撤仍深，代表上方套牢壓力存在。"
        if (stock.debtRatio > 0.55) reasons += "負債比偏高，景氣反轉時壓力會放大。"
        if (stock.peRatio > 28) reasons += "本益比已高，評價面需要更多成長來支撐。"
        if (stock.monthlyRevenueGrowth < 0.05) reasons += "營收動能趨緩，短線催化劑不足。"
        if (riskScore >= 70) reasons += "整體風險偏高，比較適合短線而非無腦抱波段。"
        return reasons.take(4).ifEmpty { listOf("暫時沒有明顯結構性風險，但仍要持續追蹤。") }
    }

    private fun buildSummary(
        stockName: String,
        quadrant: RiskRewardQuadrant,
        starRating: Int
    ): String = when (quadrant) {
        RiskRewardQuadrant.HIGH_RISK_HIGH_REWARD ->
            "$stockName 屬於高波動但也有高成長空間的類型，適合願意承擔拉回風險、換取較大報酬彈性的策略。整體評等 $starRating 星。"
        RiskRewardQuadrant.LOW_RISK_HIGH_REWARD ->
            "$stockName 在目前名單裡是相對平衡的選項，兼具防守與上行空間，適合當核心觀察股。整體評等 $starRating 星。"
        RiskRewardQuadrant.HIGH_RISK_LOW_REWARD ->
            "$stockName 的風險偏高，但回報空間暫時沒有同步放大，除非有新的催化劑，否則不算漂亮的交易結構。整體評等 $starRating 星。"
        RiskRewardQuadrant.LOW_RISK_LOW_REWARD ->
            "$stockName 目前屬於偏穩定但爆發力有限的類型，較適合保守觀察，不急著追價。整體評等 $starRating 星。"
        RiskRewardQuadrant.ALL -> stockName
    }

    private fun percent(value: Double): String = "%+.1f%%".format(value * 100)

    private fun normalizePositive(value: Double, upperBound: Double): Double {
        return (value / upperBound).coerceIn(0.0, 1.0)
    }

    private fun normalizeNegative(value: Double, upperBound: Double): Double {
        return 1.0 - normalizePositive(value, upperBound)
    }
}
