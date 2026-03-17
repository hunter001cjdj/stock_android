package com.example.twstockanalyzer.data.repository

import com.example.twstockanalyzer.domain.model.MetricItem
import com.example.twstockanalyzer.domain.model.RiskRewardQuadrant
import com.example.twstockanalyzer.domain.model.StockAnalysis
import com.example.twstockanalyzer.domain.model.StockSector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs
import kotlin.math.roundToInt

class TwseHybridStockRepository : StockRepository {
    private var cached: List<StockAnalysis> = emptyList()

    override fun getStocks(): List<StockAnalysis> = cached

    override suspend fun refreshStocks(): List<StockAnalysis> = withContext(Dispatchers.IO) {
        val favoriteIds = cached.filter { it.isFavorite }.map { it.stockId }.toSet()
        val quotes = loadQuotes()
        val valuations = loadValuations()

        val refreshed = quotes
            .asSequence()
            .filter { it.code.length == 4 && it.closingPrice != null }
            .mapNotNull { quote ->
                buildAnalysis(
                    quote = quote,
                    valuation = valuations[quote.code],
                    isFavorite = quote.code in favoriteIds
                )
            }
            .sortedByDescending { it.finalScore }
            .take(80)
            .toList()

        cached = refreshed
        refreshed
    }

    private fun loadQuotes(): List<TwseQuoteRow> {
        val array = fetchJsonArray("https://openapi.twse.com.tw/v1/exchangeReport/STOCK_DAY_ALL")
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            TwseQuoteRow(
                code = item.optString("Code"),
                name = item.optString("Name"),
                volume = item.optString("TradeVolume").toLongOrNull() ?: 0L,
                closingPrice = item.optString("ClosingPrice").toDoubleOrNull(),
                changeAmount = item.optString("Change").toDoubleOrNull()
            )
        }
    }

    private fun loadValuations(): Map<String, TwseValuationRow> {
        val array = fetchJsonArray("https://openapi.twse.com.tw/v1/exchangeReport/BWIBBU_ALL")
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            TwseValuationRow(
                code = item.optString("Code"),
                dividendYield = item.optString("DividendYield").toDoubleOrNull(),
                peRatio = item.optString("PEratio").toDoubleOrNull(),
                pbRatio = item.optString("PBratio").toDoubleOrNull()
            )
        }.associateBy { it.code }
    }

    private fun buildAnalysis(
        quote: TwseQuoteRow,
        valuation: TwseValuationRow?,
        isFavorite: Boolean
    ): StockAnalysis? {
        val price = quote.closingPrice ?: return null
        val changePercent = quote.changePercent()
        val dividendYield = valuation?.dividendYield
        val peRatio = valuation?.peRatio
        val pbRatio = valuation?.pbRatio

        val rewardScore = (
            positiveScore(changePercent, 7.5) * 0.45 +
                positiveScore(dividendYield, 8.0) * 0.25 +
                invertedScore(peRatio, 30.0) * 0.15 +
                positiveScore(quote.volume.toDouble(), 80_000_000.0) * 0.15
            ).times(100).roundToInt().coerceIn(0, 100)

        val riskScore = (
            positiveScore(abs(changePercent ?: 0.0), 7.5) * 0.40 +
                positiveScore(peRatio, 35.0) * 0.25 +
                positiveScore(pbRatio, 4.0) * 0.20 +
                invertedScore(quote.volume.toDouble(), 20_000_000.0) * 0.15
            ).times(100).roundToInt().coerceIn(0, 100)

        val finalScore = (
            rewardScore * 0.55 +
                (100 - riskScore) * 0.35 +
                positiveScore(quote.volume.toDouble(), 80_000_000.0) * 100 * 0.10
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

        val recommendReasons = mutableListOf<String>()
        val avoidReasons = mutableListOf<String>()

        if ((changePercent ?: 0.0) > 2.0) recommendReasons += "當日漲幅有動能，短線資金願意追價。"
        if ((dividendYield ?: 0.0) >= 4.0) recommendReasons += "殖利率不差，具備一定防守力。"
        if ((peRatio ?: 999.0) in 0.0..18.0) recommendReasons += "本益比仍在相對合理區間。"
        if (quote.volume >= 20_000_000L) recommendReasons += "成交量夠大，流動性較好。"

        if ((changePercent ?: 0.0) < -2.5) avoidReasons += "當日跌幅偏大，短線情緒可能還沒穩。"
        if ((peRatio ?: 0.0) > 28.0) avoidReasons += "本益比偏高，需要更強成長支持。"
        if ((pbRatio ?: 0.0) > 3.0) avoidReasons += "股價淨值比偏高，評價壓力較大。"
        if (quote.volume < 2_000_000L) avoidReasons += "成交量偏低，進出容易不順。"

        if (recommendReasons.isEmpty()) recommendReasons += "官方即時可得指標偏中性，建議搭配更多基本面資料再判斷。"
        if (avoidReasons.isEmpty()) avoidReasons += "目前沒有特別極端的風險訊號，但仍要持續追蹤。"

        val summary = when (quadrant) {
            RiskRewardQuadrant.HIGH_RISK_HIGH_REWARD ->
                "${quote.name} 目前波動和上行空間都偏高，比較像高彈性交易型標的。"
            RiskRewardQuadrant.LOW_RISK_HIGH_REWARD ->
                "${quote.name} 的風險相對可控，報酬條件也不差，適合優先觀察。"
            RiskRewardQuadrant.HIGH_RISK_LOW_REWARD ->
                "${quote.name} 目前承擔的風險偏高，但回報條件還不夠漂亮。"
            RiskRewardQuadrant.LOW_RISK_LOW_REWARD ->
                "${quote.name} 屬於穩定但爆發力有限的型態，適合保守追蹤。"
            RiskRewardQuadrant.ALL -> quote.name
        }

        return StockAnalysis(
            stockId = quote.code,
            stockName = quote.name,
            sector = resolveSector(quote.code, quote.name),
            priceLabel = "NT$ %.2f".format(price),
            dailyChangeLabel = "%+.2f%%".format(changePercent ?: 0.0),
            starRating = starRating,
            finalScore = finalScore,
            riskScore = riskScore,
            rewardScore = rewardScore,
            quadrant = quadrant,
            recommendReasons = recommendReasons.take(4),
            avoidReasons = avoidReasons.take(4),
            summary = summary,
            metrics = listOf(
                MetricItem("收盤價", "NT$ %.2f".format(price)),
                MetricItem("漲跌幅", "%+.2f%%".format(changePercent ?: 0.0)),
                MetricItem("本益比", peRatio?.let { "%.2f".format(it) } ?: "無資料"),
                MetricItem("股價淨值比", pbRatio?.let { "%.2f".format(it) } ?: "無資料"),
                MetricItem("殖利率", dividendYield?.let { "%.2f%%".format(it) } ?: "無資料"),
                MetricItem("成交量", formatVolume(quote.volume))
            ),
            isFavorite = isFavorite
        )
    }

    private fun resolveSector(code: String, name: String): StockSector {
        return when {
            code in setOf("2317", "3231", "6669", "2382", "4938", "3017") ||
                name.contains("伺服器") || name.contains("AI") || name.contains("鴻海") -> StockSector.AI_SERVER

            code in setOf("2330", "2303", "2454", "3711", "3034", "3443", "5347") ||
                name.contains("半導體") || name.contains("晶") || name.contains("矽") || name.contains("聯發") -> StockSector.SEMICONDUCTOR

            code.startsWith("28") ||
                name.contains("金控") || name.contains("銀行") || name.contains("證券") || name.contains("保險") || name.contains("票券") -> StockSector.FINANCE

            code.startsWith("26") ||
                name.contains("航運") || name.contains("貨櫃") || name.contains("海運") || name.contains("航空") || name.contains("港") -> StockSector.SHIPPING

            code.startsWith("00") ||
                name.contains("ETF") || name.contains("台灣50") || name.contains("高股息") -> StockSector.ETF

            code.startsWith("23") || code.startsWith("24") || code.startsWith("30") || code.startsWith("36") ||
                name.contains("電子") || name.contains("科技") || name.contains("光電") || name.contains("網通") || name.contains("電腦") -> StockSector.TECH

            code.startsWith("11") || code.startsWith("12") || code.startsWith("13") || code.startsWith("14") ||
                code.startsWith("15") || code.startsWith("17") || code.startsWith("18") || code.startsWith("20") ||
                code.startsWith("21") || code.startsWith("22") || code.startsWith("25") -> StockSector.TRADITIONAL

            else -> StockSector.OTHER
        }
    }

    private fun fetchJsonArray(url: String): JSONArray {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "TWStockAnalyzer/1.0")
        }

        connection.inputStream.use { stream ->
            val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
            return JSONArray(text)
        }
    }

    private fun positiveScore(value: Double?, upperBound: Double): Double {
        if (value == null) return 0.5
        return (value / upperBound).coerceIn(0.0, 1.0)
    }

    private fun invertedScore(value: Double?, upperBound: Double): Double {
        if (value == null) return 0.5
        return 1.0 - positiveScore(value, upperBound)
    }

    private fun formatVolume(volume: Long): String {
        return when {
            volume >= 100_000_000L -> "%.2f 億".format(volume / 100_000_000.0)
            volume >= 10_000L -> "%.2f 萬".format(volume / 10_000.0)
            else -> volume.toString()
        }
    }
}

private data class TwseQuoteRow(
    val code: String,
    val name: String,
    val volume: Long,
    val closingPrice: Double?,
    val changeAmount: Double?
) {
    fun changePercent(): Double? {
        val price = closingPrice ?: return null
        val change = changeAmount ?: return null
        val previous = price - change
        if (previous == 0.0) return null
        return (change / previous) * 100
    }
}

private data class TwseValuationRow(
    val code: String,
    val dividendYield: Double?,
    val peRatio: Double?,
    val pbRatio: Double?
)
