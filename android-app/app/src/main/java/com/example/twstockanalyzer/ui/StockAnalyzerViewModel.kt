package com.example.twstockanalyzer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twstockanalyzer.data.repository.StockRepository
import com.example.twstockanalyzer.domain.model.StockAnalysis
import com.example.twstockanalyzer.domain.model.StockSector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StockAnalyzerViewModel(
    private val repository: StockRepository
) : ViewModel() {
    private var allStocks = repository.getStocks()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val _uiState = MutableStateFlow(
        StockAnalyzerUiState(
            stocks = allStocks,
            featuredStocks = allStocks.take(6),
            favoriteStocks = allStocks.filter { it.isFavorite },
            selectedStock = allStocks.firstOrNull(),
            marketPulse = buildMarketPulse(allStocks),
            lastUpdatedLabel = "等待讀取真實資料",
            aiMessages = listOf(
                AiMessage(
                    id = 1,
                    isUser = false,
                    text = "我是台股 AI 助理。你可以先從產業分類挑股票，再用收藏與 AI 分析快速縮小研究範圍。"
                )
            )
        )
    )
    val uiState: StateFlow<StockAnalyzerUiState> = _uiState.asStateFlow()

    init {
        refreshSnapshot()
    }

    fun selectTab(tab: UiTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectSector(sector: StockSector) {
        _uiState.update { current ->
            val filtered = filterStocks(sector, allStocks)
            current.copy(
                selectedSector = sector,
                stocks = filtered,
                featuredStocks = buildFeaturedStocks(sector),
                selectedStock = filtered.firstOrNull() ?: current.selectedStock
            )
        }
    }

    fun selectStock(stockId: String) {
        _uiState.update { current ->
            current.copy(
                selectedStock = allStocks.firstOrNull { it.stockId == stockId } ?: current.selectedStock
            )
        }
    }

    fun toggleFavorite(stockId: String) {
        allStocks = allStocks.map { stock ->
            if (stock.stockId == stockId) stock.copy(isFavorite = !stock.isFavorite) else stock
        }
        syncState()
    }

    fun refreshSnapshot() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, refreshError = null) }
            runCatching { repository.refreshStocks() }
                .onSuccess { refreshed ->
                    allStocks = refreshed
                    syncState(sourceLabel = "TWSE 官方開放資料")
                }
                .onFailure {
                    _uiState.update { current ->
                        current.copy(
                            isRefreshing = false,
                            refreshError = "讀取失敗，請稍後再試一次。",
                            lastUpdatedLabel = "讀取失敗"
                        )
                    }
                }
        }
    }

    fun askAi(prompt: String) {
        val userId = nextMessageId()
        val assistantId = userId + 1
        val userMessage = AiMessage(id = userId, isUser = true, text = prompt)
        val assistantMessage = AiMessage(id = assistantId, isUser = false, text = buildAiReply(prompt))

        _uiState.update { current ->
            current.copy(
                currentTab = UiTab.AI,
                aiMessages = current.aiMessages + userMessage + assistantMessage
            )
        }
    }

    private fun syncState(sourceLabel: String = _uiState.value.dataSourceLabel) {
        val sector = _uiState.value.selectedSector
        val filtered = filterStocks(sector, allStocks)
        val selected = allStocks.firstOrNull { it.stockId == _uiState.value.selectedStock?.stockId }
            ?: filtered.firstOrNull()

        _uiState.update { current ->
            current.copy(
                stocks = filtered,
                featuredStocks = buildFeaturedStocks(sector),
                favoriteStocks = allStocks.filter { it.isFavorite },
                selectedStock = selected,
                marketPulse = buildMarketPulse(allStocks),
                lastUpdatedLabel = "更新於 ${LocalDateTime.now().format(timeFormatter)}",
                dataSourceLabel = sourceLabel,
                isRefreshing = false
            )
        }
    }

    private fun buildFeaturedStocks(sector: StockSector): List<StockAnalysis> {
        val source = if (sector == StockSector.ALL) allStocks else filterStocks(sector, allStocks)
        return source.sortedByDescending { it.finalScore }.take(6)
    }

    private fun filterStocks(
        sector: StockSector,
        stocks: List<StockAnalysis>
    ): List<StockAnalysis> {
        return stocks.filter {
            sector == StockSector.ALL || it.sector == sector
        }
    }

    private fun buildMarketPulse(stocks: List<StockAnalysis>): String {
        if (stocks.isEmpty()) {
            return "目前尚未讀到官方真實資料。請按右上角刷新，或稍後再試一次。"
        }

        val topScore = stocks.maxByOrNull { it.finalScore }
        val topGain = stocks.maxByOrNull { parsePercent(it.dailyChangeLabel) }
        val hottestSector = stocks
            .groupBy { it.sector }
            .maxByOrNull { (_, items) -> items.sumOf { it.finalScore } }
            ?.key

        return buildString {
            append("目前可分析 ${stocks.size} 檔。")
            topScore?.let { append(" 綜合分數最高是 ${it.stockName}。") }
            topGain?.let { append(" 當日動能最強是 ${it.stockName}。") }
            hottestSector?.let { append(" 熱度最高的產業是 ${it.label}。") }
        }
    }

    private fun buildAiReply(prompt: String): String {
        val selected = _uiState.value.selectedStock
        val favorites = _uiState.value.favoriteStocks.sortedByDescending { it.finalScore }
        val sector = _uiState.value.selectedSector

        if (selected == null) {
            return "目前還沒有可分析的個股。請先刷新真實資料，或切到有資料的產業分類後再試。"
        }

        return when {
            prompt.contains("最穩") -> {
                val safest = (_uiState.value.favoriteStocks.ifEmpty { allStocks }).minByOrNull { it.riskScore } ?: selected
                "以目前資料看，較穩健的是 ${safest.stockName}，風險分數 ${safest.riskScore}，但仍要一起看成交量與估值。"
            }

            prompt.contains("收藏") -> {
                if (favorites.isEmpty()) {
                    "你目前還沒有收藏股。可以先在 ${sector.label} 分類中挑幾檔，再讓我幫你比較。"
                } else {
                    val topFavorite = favorites.first()
                    "你收藏裡目前最值得先看的，是 ${topFavorite.stockName}，綜合分數 ${topFavorite.finalScore}，屬於 ${topFavorite.sector.label}。"
                }
            }

            prompt.contains("風險") -> {
                "以 ${selected.stockName} 來看，主要風險是 ${selected.avoidReasons.firstOrNull() ?: "短線波動與估值變化"}。如果要介入，建議搭配刷新後的成交量一起看。"
            }

            else -> {
                "目前選中的 ${selected.stockName} 屬於 ${selected.sector.label}，綜合分數 ${selected.finalScore}，風險 ${selected.riskScore}、報酬 ${selected.rewardScore}。最值得先看的重點是 ${selected.recommendReasons.firstOrNull() ?: "基本面與價格動能"}。"
            }
        }
    }

    private fun parsePercent(label: String): Double {
        return label.removePrefix("+").removeSuffix("%").toDoubleOrNull() ?: 0.0
    }

    private fun nextMessageId(): Long = (_uiState.value.aiMessages.maxOfOrNull { it.id } ?: 0L) + 1
}

enum class UiTab(val label: String) {
    DASHBOARD("總覽"),
    FAVORITES("收藏"),
    AI("AI 分析")
}

data class AiMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String
)

data class StockAnalyzerUiState(
    val currentTab: UiTab = UiTab.DASHBOARD,
    val selectedSector: StockSector = StockSector.ALL,
    val stocks: List<StockAnalysis> = emptyList(),
    val featuredStocks: List<StockAnalysis> = emptyList(),
    val favoriteStocks: List<StockAnalysis> = emptyList(),
    val selectedStock: StockAnalysis? = null,
    val marketPulse: String = "",
    val lastUpdatedLabel: String = "",
    val dataSourceLabel: String = "本地快取",
    val isRefreshing: Boolean = false,
    val refreshError: String? = null,
    val aiPrompts: List<String> = listOf(
        "目前哪一檔最穩？",
        "幫我比較收藏股",
        "這檔最大的風險是什麼？"
    ),
    val aiMessages: List<AiMessage> = emptyList()
) {
    val latestAiMessage: AiMessage?
        get() = aiMessages.lastOrNull { !it.isUser }
}
