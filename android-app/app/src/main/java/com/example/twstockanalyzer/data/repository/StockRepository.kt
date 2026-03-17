package com.example.twstockanalyzer.data.repository

import com.example.twstockanalyzer.domain.model.StockAnalysis

interface StockRepository {
    fun getStocks(): List<StockAnalysis>
    suspend fun refreshStocks(): List<StockAnalysis>
}
