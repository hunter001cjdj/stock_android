package com.example.twstockanalyzer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.twstockanalyzer.domain.model.MetricItem
import com.example.twstockanalyzer.domain.model.StockAnalysis
import com.example.twstockanalyzer.domain.model.StockSector
import com.example.twstockanalyzer.ui.theme.CardWhite
import com.example.twstockanalyzer.ui.theme.GlassBorder
import com.example.twstockanalyzer.ui.theme.GlassCyan
import com.example.twstockanalyzer.ui.theme.InkBlue
import com.example.twstockanalyzer.ui.theme.NegativeRed
import com.example.twstockanalyzer.ui.theme.PositiveGreen
import com.example.twstockanalyzer.ui.theme.SoftGray

@Composable
fun StockAnalyzerApp(viewModel: StockAnalyzerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF07111F),
            Color(0xFF0A1C31),
            Color(0xFF0F2E4E)
        )
    )

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DashboardHeader(
                        favoriteCount = uiState.favoriteStocks.size,
                        lastUpdatedLabel = uiState.lastUpdatedLabel,
                        dataSourceLabel = uiState.dataSourceLabel,
                        refreshError = uiState.refreshError,
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refreshSnapshot
                    )
                }
                item {
                    AppTabs(
                        currentTab = uiState.currentTab,
                        onSelect = viewModel::selectTab
                    )
                }

                when (uiState.currentTab) {
                    UiTab.DASHBOARD -> {
                        item {
                            MarketPulseCard(
                                marketPulse = uiState.marketPulse,
                                selectedSector = uiState.selectedSector.label
                            )
                        }
                        item {
                            SectorSection(
                                selectedSector = uiState.selectedSector,
                                onSelect = viewModel::selectSector
                            )
                        }
                        item {
                            FeaturedSection(
                                stocks = uiState.featuredStocks,
                                onSelect = viewModel::selectStock
                            )
                        }
                        item {
                            SummaryStrip(
                                selectedSector = uiState.selectedSector.label,
                                stockCount = uiState.stocks.size,
                                favoriteCount = uiState.favoriteStocks.size
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

                    UiTab.FAVORITES -> {
                        item {
                            FavoritesOverview(
                                favorites = uiState.favoriteStocks,
                                onSelect = {
                                    viewModel.selectStock(it)
                                    viewModel.selectTab(UiTab.DASHBOARD)
                                },
                                onToggleFavorite = viewModel::toggleFavorite
                            )
                        }
                    }

                    UiTab.AI -> {
                        item {
                            AiControlPanel(
                                selectedStock = uiState.selectedStock,
                                prompts = uiState.aiPrompts,
                                latestMessage = uiState.latestAiMessage,
                                onPromptClick = viewModel::askAi
                            )
                        }
                        items(uiState.aiMessages.asReversed()) { message ->
                            AiMessageBubble(message = message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    favoriteCount: Int,
    lastUpdatedLabel: String,
    dataSourceLabel: String,
    refreshError: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    GlassCard(shape = RoundedCornerShape(28.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "台股 AI 即時儀表板",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "依產業切換股票池，把收藏、即時資料與 AI 摘要放在同一個入口。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "刷新",
                        tint = GlassCyan
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeaderPill(text = "收藏 $favoriteCount 檔")
                HeaderPill(text = lastUpdatedLabel)
                HeaderPill(text = dataSourceLabel)
                if (isRefreshing) HeaderPill(text = "更新中")
            }

            refreshError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFA8A8)
                )
            }
        }
    }
}

@Composable
private fun AppTabs(
    currentTab: UiTab,
    onSelect: (UiTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UiTab.entries.forEach { tab ->
            FilterChip(
                selected = currentTab == tab,
                onClick = { onSelect(tab) },
                label = { Text(tab.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White.copy(alpha = 0.16f),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.06f),
                    labelColor = Color.White.copy(alpha = 0.8f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentTab == tab,
                    borderColor = GlassBorder,
                    selectedBorderColor = GlassCyan
                )
            )
        }
    }
}

@Composable
private fun MarketPulseCard(
    marketPulse: String,
    selectedSector: String
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timeline,
                    contentDescription = null,
                    tint = GlassCyan
                )
                Text(
                    text = "市場快照",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = marketPulse,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f)
            )
            LabelPill(text = "目前分類：$selectedSector")
        }
    }
}

@Composable
private fun SectorSection(
    selectedSector: StockSector,
    onSelect: (StockSector) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(
            title = "產業分類",
            subtitle = "依金融、航運、科技、半導體等產業切換觀察名單"
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StockSector.entries.forEach { sector ->
                FilterChip(
                    selected = selectedSector == sector,
                    onClick = { onSelect(sector) },
                    label = { Text(sector.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0EB6FF).copy(alpha = 0.22f),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White.copy(alpha = 0.82f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedSector == sector,
                        borderColor = GlassBorder,
                        selectedBorderColor = GlassCyan
                    )
                )
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
            title = "今日焦點清單",
            subtitle = "先看各產業裡分數與動能都突出的標的"
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stock.stockName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${stock.stockId} ・ ${stock.sector.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                        RatingRow(stock.starRating)
                        Text(
                            text = "綜合分數 ${stock.finalScore}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStrip(
    selectedSector: String,
    stockCount: Int,
    favoriteCount: Int
) {
    GlassCard(shape = RoundedCornerShape(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryStat(label = "目前產業", value = selectedSector)
            SummaryStat(label = "可分析", value = "$stockCount 檔")
            SummaryStat(label = "已收藏", value = "$favoriteCount 檔")
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.68f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, GlassBorder)
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
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${stock.sector.label} ・ ${stock.priceLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                    Text(
                        text = stock.dailyChangeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (stock.dailyChangeLabel.startsWith("+")) PositiveGreen else NegativeRed
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (stock.isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "收藏",
                        tint = if (stock.isFavorite) GlassCyan else Color.White.copy(alpha = 0.72f)
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabelPill(stock.sector.label)
                LabelPill("綜合 ${stock.finalScore}")
                LabelPill("風險 ${stock.riskScore}")
                LabelPill("報酬 ${stock.rewardScore}")
            }

            RatingRow(stock.starRating)
            ReasonPreview(title = "值得留意", items = stock.recommendReasons)
            ReasonPreview(title = "風險提醒", items = stock.avoidReasons)
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
            title = "個股詳情",
            subtitle = "把摘要、核心指標與風險一起集中檢視"
        )

        GlassCard(shape = RoundedCornerShape(28.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${stock.stockName} (${stock.stockId})",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stock.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (stock.isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "收藏",
                            tint = if (stock.isFavorite) GlassCyan else Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelPill(stock.sector.label)
                    LabelPill("${stock.starRating} 星")
                }

                MetricGrid(stock.metrics)
                ReasonBlock("支持理由", stock.recommendReasons)
                ReasonBlock("風險提醒", stock.avoidReasons)
            }
        }
    }
}

@Composable
private fun FavoritesOverview(
    favorites: List<StockAnalysis>,
    onSelect: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(
            title = "收藏清單",
            subtitle = "集中查看你想追蹤的產業與個股"
        )

        if (favorites.isEmpty()) {
            EmptyStateCard(
                title = "還沒有收藏股",
                description = "回到總覽後點選書籤，就能把個股加入你的觀察名單。"
            )
        } else {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "共 ${favorites.size} 檔收藏",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    favorites.forEach { stock ->
                        FavoriteRow(
                            stock = stock,
                            onClick = { onSelect(stock.stockId) },
                            onToggleFavorite = { onToggleFavorite(stock.stockId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    stock: StockAnalysis,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${stock.stockName} (${stock.stockId})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${stock.sector.label} ・ ${stock.priceLabel} ・ ${stock.dailyChangeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.74f)
            )
            Text(
                text = "綜合分數 ${stock.finalScore}",
                style = MaterialTheme.typography.bodySmall,
                color = GlassCyan
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = Icons.Rounded.Bookmark,
                contentDescription = "取消收藏",
                tint = GlassCyan
            )
        }
    }
}

@Composable
private fun AiControlPanel(
    selectedStock: StockAnalysis?,
    prompts: List<String>,
    latestMessage: AiMessage?,
    onPromptClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(
            title = "AI 分析助理",
            subtitle = "根據目前選中的產業與個股，快速得到一版可讀摘要"
        )

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = GlassCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "目前分析：${selectedStock?.stockName ?: "尚未選股"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = selectedStock?.summary ?: "先到總覽選一檔股票，再回來看 AI 摘要會更準。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        latestMessage?.let { message ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "最新分析",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkBlue
                    )
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftGray
                    )
                }
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.forEach { prompt ->
                AssistChip(
                    onClick = { onPromptClick(prompt) },
                    label = { Text(prompt) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = GlassBorder
                    )
                )
            }
        }
    }
}

@Composable
private fun AiMessageBubble(message: AiMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.width(300.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF11304B) else Color.White.copy(alpha = 0.1f)
            ),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (message.isUser) "你" else "AI 助理",
                    style = MaterialTheme.typography.labelMedium,
                    color = GlassCyan
                )
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.76f)
            )
        }
    }
}

@Composable
private fun MetricGrid(metrics: List<MetricItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "核心指標",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = metric.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = metric.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GlassCyan)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.84f)
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
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        items.take(2).forEach { item ->
            Text(
                text = "- $item",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.76f)
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
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f)
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
                tint = if (index < starRating) GlassCyan else Color.White.copy(alpha = 0.16f)
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
            containerColor = Color.White.copy(alpha = 0.08f),
            labelColor = Color.White
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = GlassBorder
        )
    )
}

@Composable
private fun HeaderPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, GlassBorder, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun GlassCard(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
