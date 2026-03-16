package com.example.twstockanalyzer.ui

import androidx.lifecycle.ViewModel
import com.example.twstockanalyzer.data.repository.StockRepository
import com.example.twstockanalyzer.domain.model.RiskRewardQuadrant
import com.example.twstockanalyzer.domain.model.StockAnalysis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StockAnalyzerViewModel(
    repository: StockRepository
) : ViewModel() {
    private var allStocks = repository.getStocks()

    private val _uiState = MutableStateFlow(
        StockAnalyzerUiState(
            stocks = allStocks,
            featuredStocks = allStocks.take(3),
            selectedStock = allStocks.firstOrNull()
        )
    )
    val uiState: StateFlow<StockAnalyzerUiState> = _uiState.asStateFlow()

    fun selectQuadrant(quadrant: RiskRewardQuadrant) {
        _uiState.update { current ->
            val filtered = allStocks.filter {
                quadrant == RiskRewardQuadrant.ALL || it.quadrant == quadrant
            }
            current.copy(
                selectedQuadrant = quadrant,
                stocks = filtered,
                selectedStock = filtered.firstOrNull() ?: current.selectedStock
            )
        }
    }

    fun selectStock(stockId: String) {
        _uiState.update { current ->
            current.copy(
                selectedStock = current.stocks.firstOrNull { it.stockId == stockId }
                    ?: allStocks.firstOrNull { it.stockId == stockId }
                    ?: current.selectedStock
            )
        }
    }

    fun toggleFavorite(stockId: String) {
        allStocks = allStocks.map { stock ->
            if (stock.stockId == stockId) stock.copy(isFavorite = !stock.isFavorite) else stock
        }

        val currentQuadrant = _uiState.value.selectedQuadrant
        val filtered = allStocks.filter {
            currentQuadrant == RiskRewardQuadrant.ALL || it.quadrant == currentQuadrant
        }
        val selected = allStocks.firstOrNull { it.stockId == _uiState.value.selectedStock?.stockId }

        _uiState.value = _uiState.value.copy(
            stocks = filtered,
            featuredStocks = allStocks.take(3),
            selectedStock = selected
        )
    }
}

data class StockAnalyzerUiState(
    val selectedQuadrant: RiskRewardQuadrant = RiskRewardQuadrant.ALL,
    val stocks: List<StockAnalysis> = emptyList(),
    val featuredStocks: List<StockAnalysis> = emptyList(),
    val selectedStock: StockAnalysis? = null
)
