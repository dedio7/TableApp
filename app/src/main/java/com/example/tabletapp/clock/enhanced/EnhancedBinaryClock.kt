package com.example.tabletapp.clock.enhanced

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EnhancedBinaryClock(
    modifier: Modifier = Modifier,
    config: ClockConfig = ClockConfig()
) {
    var columnsData by remember { mutableStateOf(ClockLogic.getColumns(config.mode, config.showSeconds)) }
    var selectedColumnIndex by remember { mutableIntStateOf(-1) }

    // Update clock every second
    LaunchedEffect(config.mode, config.showSeconds) {
        while (true) {
            columnsData = ClockLogic.getColumns(config.mode, config.showSeconds)
            delay(1000L)
        }
    }

    // Auto-hide decimal value after selection
    LaunchedEffect(selectedColumnIndex) {
        if (selectedColumnIndex != -1) {
            delay(3000L)
            selectedColumnIndex = -1
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val columnCount = columnsData.size
        val colSpacingRatio = if (config.mode == ClockMode.BCD) 0.02f else 0.05f
        val colSpacingPx = width * colSpacingRatio
        
        // Calculate dot size based on available space
        val dotSizePx = ((width - colSpacingPx * (columnCount + 1)) / columnCount)
            .coerceAtMost(height * 0.1f)
        val dotGapPx = dotSizePx * 0.35f
        
        val totalWPx = columnCount * dotSizePx + (columnCount - 1) * colSpacingPx
        val startXPx = (width - totalWPx) / 2f

        val density = LocalDensity.current

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = with(density) { (startXPx).toDp() }),
            horizontalArrangement = Arrangement.spacedBy(with(density) { colSpacingPx.toDp() })
        ) {
            columnsData.forEachIndexed { colIdx, column ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures { selectedColumnIndex = colIdx }
                        }
                ) {
                    ClockColumnComponent(
                        column = column,
                        config = config,
                        showValue = selectedColumnIndex == colIdx,
                        dotSizePx = dotSizePx,
                        dotGapPx = dotGapPx
                    )
                }
            }
        }
    }
}
