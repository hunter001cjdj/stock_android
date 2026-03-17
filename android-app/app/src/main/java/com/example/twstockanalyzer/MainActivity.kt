package com.example.twstockanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.twstockanalyzer.data.repository.TwseHybridStockRepository
import com.example.twstockanalyzer.ui.StockAnalyzerApp
import com.example.twstockanalyzer.ui.StockAnalyzerViewModel
import com.example.twstockanalyzer.ui.theme.TwStockAnalyzerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = StockAnalyzerViewModel(
            repository = TwseHybridStockRepository()
        )

        setContent {
            TwStockAnalyzerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StockAnalyzerApp(viewModel = viewModel)
                }
            }
        }
    }
}
