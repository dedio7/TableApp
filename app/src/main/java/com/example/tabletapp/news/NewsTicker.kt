package com.example.tabletapp.news

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * A horizontally scrolling news ticker composable that displays headlines
 * from multiple RSS sources with smooth infinite scrolling animation.
 *
 * Features:
 * - Semi-transparent dark background bar
 * - Source names in accent color
 * - Dot separators between headlines
 * - Auto-refresh every 30 minutes
 * - Graceful loading and error states
 *
 * @param modifier Modifier for the ticker container.
 * @param textColor Color for headline text.
 */
@Composable
fun NewsTicker(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    val accentColor = Color(0xFF4FC3F7) // Light blue accent for source names
    val separatorColor = Color(0xAAFFFFFF) // Semi-transparent white for dots
    val backgroundColor = Color(0xFF000000) // Solid black background for maximum contrast and readability

    val newsItems: State<List<NewsItem>> = remember { mutableStateOf(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val hasError = remember { mutableStateOf(false) }

    val repository = remember { NewsRepository() }

    // Fetch news and auto-refresh every 30 minutes (or 10 seconds if it fails)
    LaunchedEffect(Unit) {
        while (isActive) {
            isLoading.value = true
            var success = false
            try {
                val items = repository.fetchNews()
                (newsItems as androidx.compose.runtime.MutableState).value = items
                val empty = items.isEmpty()
                hasError.value = empty
                success = !empty
            } catch (e: Exception) {
                hasError.value = true
                success = false
            }
            isLoading.value = false
            if (success) {
                // Wait 30 minutes before next refresh
                delay(30L * 60L * 1000L)
            } else {
                // Wait 10 seconds then retry
                delay(10L * 1000L)
            }
        }
    }

    // Build the ticker text
    val tickerText: AnnotatedString = remember(newsItems.value) {
        if (newsItems.value.isEmpty()) {
            AnnotatedString("")
        } else {
            buildTickerText(newsItems.value, accentColor, textColor, separatorColor)
        }
    }

    val textStyle = TextStyle(
        fontSize = 20.sp, // Increased font size for better readability
        letterSpacing = 0.5.sp
    )

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Measure the full ticker text width
    val textLayoutResult = remember(tickerText, textStyle) {
        if (tickerText.text.isNotEmpty()) {
            textMeasurer.measure(tickerText, textStyle)
        } else {
            null
        }
    }

    val textWidthPx = textLayoutResult?.size?.width?.toFloat() ?: 0f

    // Screen width approximation: we'll use a large cycle width
    // The animation scrolls from right edge to -textWidth
    val totalScrollDistance = remember(textWidthPx) {
        if (textWidthPx > 0f) textWidthPx + 2000f else 4000f
    }

    // Animation duration scales with text length for consistent speed
    // ~12 pixels per second for readable scrolling (reduced from 30f to be much slower and very readable)
    val durationMs = remember(totalScrollDistance) {
        ((totalScrollDistance / 12f) * 1000f).toInt().coerceAtLeast(10000)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "newsTicker")
    val offsetX: State<Float> = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerScroll"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor)
            .clipToBounds(),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            isLoading.value && newsItems.value.isEmpty() -> {
                // Loading state
                val loadingText = AnnotatedString("Caricamento notizie...")
                val loadingStyle = TextStyle(
                    fontSize = 18.sp,
                    color = textColor.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    val yCenter = size.height / 2f
                    val measuredLoading = textMeasurer.measure(loadingText, loadingStyle)
                    drawText(
                        textLayoutResult = measuredLoading,
                        topLeft = Offset(0f, yCenter - measuredLoading.size.height / 2f)
                    )
                }
            }

            hasError.value && newsItems.value.isEmpty() -> {
                // Error state
                val errorText = AnnotatedString("Notizie non disponibili")
                val errorStyle = TextStyle(
                    fontSize = 18.sp,
                    color = textColor.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    val yCenter = size.height / 2f
                    val measuredError = textMeasurer.measure(errorText, errorStyle)
                    drawText(
                        textLayoutResult = measuredError,
                        topLeft = Offset(0f, yCenter - measuredError.size.height / 2f)
                    )
                }
            }

            textLayoutResult != null -> {
                // Scrolling ticker
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    val canvasWidth = size.width
                    val yCenter = size.height / 2f
                    val textHeight = textLayoutResult.size.height.toFloat()

                    // Calculate the current scroll offset
                    // offsetX.value goes from 0 to -1, we map it to canvasWidth to -textWidthPx
                    val scrollX = canvasWidth + offsetX.value * totalScrollDistance

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(scrollX, yCenter - textHeight / 2f)
                    )

                    // Draw a second copy right after the first for seamless looping
                    val secondX = scrollX + textWidthPx + with(density) { 120.dp.toPx() }
                    if (secondX < canvasWidth + textWidthPx) {
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(secondX, yCenter - textHeight / 2f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds an [AnnotatedString] for the ticker with source names in accent color,
 * headlines in text color, and dot separators between items.
 *
 * @param items The news items to display.
 * @param accentColor Color for source names.
 * @param textColor Color for headline text.
 * @param separatorColor Color for separator dots.
 * @return Formatted annotated string for the ticker.
 */
private fun buildTickerText(
    items: List<NewsItem>,
    accentColor: Color,
    textColor: Color,
    separatorColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        items.forEachIndexed { index, item ->
            // Source name in accent color with bold weight
            withStyle(
                SpanStyle(
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            ) {
                append(item.source)
            }
            // Separator between source and title
            withStyle(SpanStyle(color = separatorColor, fontSize = 20.sp)) {
                append(" | ")
            }
            // Headline text
            withStyle(SpanStyle(color = textColor, fontSize = 20.sp)) {
                append(item.title)
            }
            // Dot separator between items
            if (index < items.size - 1) {
                withStyle(SpanStyle(color = separatorColor, fontSize = 20.sp)) {
                    append("  •  ")
                }
            }
        }
    }
}
