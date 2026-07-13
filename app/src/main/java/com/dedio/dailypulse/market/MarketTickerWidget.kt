package com.dedio.dailypulse.market

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.network.NetworkClient
import com.dedio.dailypulse.network.NetworkResult
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlin.time.Duration.Companion.minutes

@Composable
fun MarketTickerWidget(
    modifier: Modifier = Modifier,
    symbols: Set<String> = setOf("BTC-USD", "ETH-USD", "AAPL", "TSLA"),
    textColor: Color = Color.White
) {
    var marketData by remember { mutableStateOf<List<StockInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(symbols) {
        while(true) {
            isLoading = marketData.isEmpty()
            val newData = symbols.mapNotNull { fetchStockPrice(it) }
            if (newData.isNotEmpty()) marketData = newData
            isLoading = false
            delay(10.minutes)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = textColor.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(marketData) { stock ->
                    StockItem(stock, textColor)
                }
            }
        }
    }
}

@Composable
private fun StockItem(stock: StockInfo, textColor: Color) {
    val trendColor = if (stock.change >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
    val sign = if (stock.change >= 0) "+" else ""

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stock.symbol,
            color = textColor.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = stock.price,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$sign${stock.change}%",
            color = trendColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private suspend fun fetchStockPrice(symbol: String): StockInfo? {
    // Using a reliable public API for stock/crypto (e.g. Yahoo Finance JSON fallback or similar)
    // For this POC, we use a simulation or a known public endpoint if available.
    // Let's use a very basic mock that looks real for the demo, or try a real ping.
    return try {
        // Mocking for now to avoid API key requirements in a generic widget
        val basePrice = when(symbol) {
            "BTC-USD" -> 65000.0
            "ETH-USD" -> 3500.0
            "AAPL" -> 210.0
            "TSLA" -> 180.0
            else -> 100.0
        }
        val randomChange = (-200..200).random() / 100.0
        StockInfo(
            symbol = symbol.substringBefore("-"),
            price = String.format("%.2f", basePrice + (basePrice * randomChange / 100.0)),
            change = randomChange
        )
    } catch (_: Exception) { null }
}

data class StockInfo(val symbol: String, val price: String, val change: Double)
