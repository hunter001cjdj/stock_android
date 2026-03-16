package com.example.twstockanalyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.twstockanalyzer.domain.model.MetricItem
import com.example.twstockanalyzer.domain.model.RiskRewardQuadrant
import com.example.twstockanalyzer.domain.model.StockAnalysis
import com.example.twstockanalyzer.ui.theme.AccentGold
import com.example.twstockanalyzer.ui.theme.InkBlue
import com.example.twstockanalyzer.ui.theme.SoftGray
import com.example.twstockanalyzer.ui.theme.SkyBlue

@Composable
fun StockAnalyzerApp(viewModel: StockAnalyzerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { AppHero() }
                item {
                    FeaturedSection(
                        stocks = uiState.featuredStocks,
                        onSelect = viewModel::selectStock
                    )
                }
                item {
                    QuadrantSection(
                        selectedQuadrant = uiState.selectedQuadrant,
                        onSelect = viewModel::selectQuadrant
                    )
                }
                item {
                    SummaryStrip(
                        selectedQuadrant = uiState.selectedQuadrant,
                        stockCount = uiState.stocks.size
                    )
                }
                items(uiState.stocks) { stock ->
                    StockListCard(
                        stock = stock,
                        onClick = { viewModel.selectStock(stock.stockId) },
                        onToggleFavorite = { viewModel.toggleFavorite(stock.stockId) }
                    )
                }
                item {
                    uiState.selectedStock?.let { stock ->
                        StockDetailSection(
                            stock = stock,
                            onToggleFavorite = { viewModel.toggleFavorite(stock.stockId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppHero() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = InkBlue)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "TW Stock Analyzer",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Analyze Taiwan stocks with a simple rule-based engine, quadrant filters, and star ratings.",
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Quadrants")
                Pill("1-5 stars")
                Pill("Why / Why not")
            }
        }
    }
}

@Composable
private fun FeaturedSection(
    stocks: List<StockAnalysis>,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(
            title = "Featured Watchlist",
            subtitle = "Top three ideas by current score"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stocks.forEach { stock ->
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .clickable { onSelect(stock.stockId) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stock.stockName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stock.stockId,
                            color = SoftGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        RatingRow(starRating = stock.starRating)
                        Text(
                            text = "Score ${stock.finalScore}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stock.quadrant.label,
                            color = InkBlue,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuadrantSection(
    selectedQuadrant: RiskRewardQuadrant,
    onSelect: (RiskRewardQuadrant) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(
            title = "Risk / Reward Filter",
            subtitle = "Switch between strategy styles"
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskRewardQuadrant.entries.forEach { quadrant ->
                FilterChip(
                    selected = selectedQuadrant == quadrant,
                    onClick = { onSelect(quadrant) },
                    label = { Text(quadrant.label) }
                )
            }
        }
    }
}

@Composable
private fun SummaryStrip(
    selectedQuadrant: RiskRewardQuadrant,
    stockCount: Int
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SkyBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current filter",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftGray
                )
                Text(
                    text = selectedQuadrant.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Matching stocks",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftGray
                )
                Text(
                    text = "$stockCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StockListCard(
    stock: StockAnalysis,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${stock.stockName} (${stock.stockId})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stock.priceLabel}  ${stock.dailyChangeLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (stock.dailyChangeLabel.startsWith("+")) Color(0xFFB8322F) else Color(0xFF197A51)
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (stock.isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (stock.isFavorite) AccentGold else SoftGray
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabelPill(stock.quadrant.label)
                LabelPill("Score ${stock.finalScore}")
                LabelPill("Risk ${stock.riskScore}")
                LabelPill("Reward ${stock.rewardScore}")
            }

            RatingRow(starRating = stock.starRating)
            ReasonPreview(title = "Why it stands out", items = stock.recommendReasons)
            ReasonPreview(title = "What to watch", items = stock.avoidReasons)
        }
    }
}

@Composable
private fun StockDetailSection(
    stock: StockAnalysis,
    onToggleFavorite: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(
            title = "Stock Detail",
            subtitle = "The selected stock appears here"
        )

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${stock.stockName} (${stock.stockId})",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stock.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftGray
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (stock.isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (stock.isFavorite) AccentGold else SoftGray
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelPill(stock.quadrant.label)
                    LabelPill("${stock.starRating} stars")
                }

                MetricGrid(metrics = stock.metrics)
                ReasonBlock(title = "Why this stock is recommended", items = stock.recommendReasons)
                ReasonBlock(title = "Why caution is still needed", items = stock.avoidReasons)
            }
        }
    }
}

@Composable
private fun MetricGrid(metrics: List<MetricItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Core metrics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        metrics.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { metric ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SkyBlue)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = metric.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftGray
                            )
                            Text(
                                text = metric.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ReasonBlock(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(InkBlue)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ReasonPreview(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        items.take(2).forEach { item ->
            Text(
                text = "- $item",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftGray
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SoftGray
        )
    }
}

@Composable
private fun RatingRow(starRating: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (index < starRating) AccentGold else Color(0xFFE2E8F0)
            )
        }
    }
}

@Composable
private fun LabelPill(text: String) {
    AssistChip(
        onClick = {},
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFFF5F7FB),
            labelColor = InkBlue
        )
    )
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
