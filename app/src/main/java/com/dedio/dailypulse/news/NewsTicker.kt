package com.dedio.dailypulse.news

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.ui.i18n.LocalStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun NewsTicker(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    refreshTrigger: Int = 0,
    refreshIntervalMs: Long = 30L * 60L * 1000L,
    enabledSources: Set<String> = emptySet(),
    language: String = "IT"
) {
    val accentColor = Color(0xFF4FC3F7)
    val separatorColor = Color(0xAAFFFFFF)
    val backgroundColor = Color(0xFF000000)
    val refreshBtnBg = Color(0xFF1A2240)
    val strings = LocalStrings.current

    var newsItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var fetchKey by remember { mutableIntStateOf(0) }
    var readerOpen by remember { mutableStateOf(false) }

    val repository = remember { NewsRepository() }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) fetchKey++
    }

    LaunchedEffect(fetchKey, enabledSources, language) {
        isLoading = true
        var success = false
        try {
            val filteredSources = if (enabledSources.isEmpty()) {
                DEFAULT_RSS_SOURCES.filter { it.language == language }
            } else {
                DEFAULT_RSS_SOURCES.filter { it.name in enabledSources }
            }
            val items = repository.fetchNews(filteredSources)
            newsItems = items
            val empty = items.isEmpty()
            hasError = empty
            success = !empty
        } catch (e: Exception) {
            hasError = true
            success = false
        }
        isLoading = false

        if (isActive) {
            val wait = if (success) refreshIntervalMs else 10_000L
            delay(wait)
            fetchKey++
        }
    }

    val tickerText: AnnotatedString = remember(newsItems) {
        if (newsItems.isEmpty()) AnnotatedString("") else buildTickerText(newsItems, accentColor, textColor, separatorColor)
    }

    val textStyle = TextStyle(fontSize = 20.sp, letterSpacing = 0.5.sp)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val textLayoutResult = remember(tickerText, textStyle) {
        if (tickerText.text.isNotEmpty()) textMeasurer.measure(tickerText, textStyle) else null
    }

    val textWidthPx = textLayoutResult?.size?.width?.toFloat() ?: 0f
    val totalScrollDistance = remember(textWidthPx) { if (textWidthPx > 0f) textWidthPx + 2000f else 4000f }
    val durationMs = remember(totalScrollDistance) { ((totalScrollDistance / 12f) * 1000f).toInt().coerceAtLeast(10000) }

    val infiniteTransition = rememberInfiniteTransition(label = "newsTicker")
    val offsetX: State<Float> = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = durationMs, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "tickerScroll"
    )

    val spinAngle: State<Float> = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1200, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "spin"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor)
            .clickable(enabled = newsItems.isNotEmpty()) { readerOpen = true },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(48.dp).padding(end = 56.dp).clipToBounds()) {
            when {
                isLoading && newsItems.isEmpty() -> {
                    val loadingStyle = TextStyle(fontSize = 18.sp, color = textColor.copy(alpha = 0.7f), letterSpacing = 0.5.sp)
                    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 16.dp)) {
                        val measured = textMeasurer.measure(AnnotatedString("..."), loadingStyle)
                        drawText(textLayoutResult = measured, topLeft = Offset(0f, size.height / 2f - measured.size.height / 2f))
                    }
                }

                hasError && newsItems.isEmpty() -> {
                    val errorStyle = TextStyle(fontSize = 18.sp, color = textColor.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 16.dp)) {
                        val msg = if (enabledSources.isEmpty()) "Seleziona fonti / Select sources" else strings.weatherNotAvailable
                        val measured = textMeasurer.measure(AnnotatedString(msg), errorStyle)
                        drawText(textLayoutResult = measured, topLeft = Offset(0f, size.height / 2f - measured.size.height / 2f))
                    }
                }

                textLayoutResult != null -> {
                    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        val canvasWidth = size.width
                        val yCenter = size.height / 2f
                        val textHeight = textLayoutResult.size.height.toFloat()
                        val scrollX = canvasWidth + offsetX.value * totalScrollDistance
                        drawText(textLayoutResult = textLayoutResult, topLeft = Offset(scrollX, yCenter - textHeight / 2f))
                        val secondX = scrollX + textWidthPx + with(density) { 120.dp.toPx() }
                        if (secondX < canvasWidth + textWidthPx) {
                            drawText(textLayoutResult = textLayoutResult, topLeft = Offset(secondX, yCenter - textHeight / 2f))
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(refreshBtnBg)
                .clickable { fetchKey++ },
            contentAlignment = Alignment.Center
        ) {
            val refreshStyle = TextStyle(fontSize = 20.sp, color = if (isLoading) accentColor else Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            val measured = remember(refreshStyle) { textMeasurer.measure(AnnotatedString("↻"), refreshStyle) }
            Canvas(modifier = Modifier.size(28.dp).then(if (isLoading) Modifier.rotate(spinAngle.value) else Modifier)) {
                drawText(textLayoutResult = measured, topLeft = Offset(size.width / 2f - measured.size.width / 2f, size.height / 2f - measured.size.height / 2f))
            }
        }
    }

    // Full News Reader Overlay
    NewsReaderPanel(
        visible = readerOpen,
        newsItems = newsItems,
        onDismiss = { readerOpen = false }
    )
}

private fun buildTickerText(items: List<NewsItem>, accentColor: Color, textColor: Color, separatorColor: Color): AnnotatedString {
    return buildAnnotatedString {
        items.forEachIndexed { index, item ->
            withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)) { append(item.source) }
            withStyle(SpanStyle(color = separatorColor, fontSize = 20.sp)) { append(" | ") }
            withStyle(SpanStyle(color = textColor, fontSize = 20.sp)) { append(item.title) }
            if (index < items.size - 1) {
                withStyle(SpanStyle(color = separatorColor, fontSize = 20.sp)) { append("  •  ") }
            }
        }
    }
}
