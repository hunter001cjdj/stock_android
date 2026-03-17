package com.example.twstockanalyzer.domain.model

data class StockAnalysis(
    val stockId: String,
    val stockName: String,
    val sector: StockSector,
    val priceLabel: String,
    val dailyChangeLabel: String,
    val starRating: Int,
    val finalScore: Int,
    val riskScore: Int,
    val rewardScore: Int,
    val quadrant: RiskRewardQuadrant,
    val recommendReasons: List<String>,
    val avoidReasons: List<String>,
    val summary: String,
    val metrics: List<MetricItem>,
    val isFavorite: Boolean = false
)

data class MetricItem(
    val label: String,
    val value: String
)
