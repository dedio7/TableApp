package com.example.tabletapp.clock.enhanced

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
        // Ridotto lo spazio tra le colonne
        val colSpacingRatio = if (config.mode == ClockMode.BCD) 0.015f else 0.03f
        val colSpacingPx = width * colSpacingRatio
        
        // Ridotta sensibilmente la dimensione dei dot (da 0.1f a 0.065f)
        // Aggiunto un limite minimo di 4px per evitare che scompaiano
        val dotSizePx = ((width - colSpacingPx * (columnCount + 2)) / columnCount)
            .coerceAtMost(height * 0.065f)
            .coerceAtLeast(4f)
            
        val dotGapPx = dotSizePx * 0.35f
        
        val density = LocalDensity.current

        // Usiamo Arrangement.Center invece del padding manuale per evitare che l'orologio "esca" dallo schermo
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            columnsData.forEachIndexed { colIdx, column ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = with(density) { (colSpacingPx / 2f).toDp() })
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
