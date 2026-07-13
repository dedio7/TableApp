package com.dedio.dailypulse.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * A beautiful, smooth temperature chart for the next 12-24 hours.
 * Improved with min/max labels and clearer indicators.
 */
@Composable
fun TemperatureChart(
    hourlyData: List<HourlyForecast>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4FC3F7),
    textColor: Color = Color.White
) {
    if (hourlyData.isEmpty()) return

    // We take the first 16 points for a good dashboard visualization
    val dataPoints = hourlyData.take(16)
    val temps = dataPoints.map { it.temperature }
    val maxTemp = temps.maxOrNull() ?: 0.0
    val minTemp = temps.minOrNull() ?: 0.0
    val tempRange = (maxTemp - minTemp).coerceAtLeast(1.0)

    val maxIndex = temps.indexOf(maxTemp)
    val minIndex = temps.indexOf(minTemp)

    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Slightly taller for labels
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacing = width / (dataPoints.size - 1)

            val points = dataPoints.mapIndexed { index, forecast ->
                val x = index * spacing
                // Normalized position (inverted Y: 0 is top)
                val normalizedTemp = (forecast.temperature - minTemp) / tempRange
                val y = height - (normalizedTemp.toFloat() * height)
                Offset(x, y)
            }

            // 1. Draw smooth curve
            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        val cp1 = Offset(p0.x + (p1.x - p0.x) / 2, p0.y)
                        val cp2 = Offset(p0.x + (p1.x - p0.x) / 2, p1.y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p1.x, p1.y)
                    }
                }
            }

            // 2. Area gradient
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.2f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // 3. Main line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 4. Markers for current, min and max
            points.forEachIndexed { index, point ->
                val isCurrent = index == 0
                val isMax = index == maxIndex
                val isMin = index == minIndex

                if (isCurrent || isMax || isMin) {
                    // Marker shadow
                    drawCircle(Color.Black.copy(alpha = 0.3f), radius = 5.dp.toPx(), center = point + Offset(0f, 2.dp.toPx()))
                    // Inner dot
                    drawCircle(
                        color = if (isMax) Color(0xFFFF7043) else if (isMin) Color(0xFF81D4FA) else Color.White,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // Labels Row: Start, Max, Min, End
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelStyle = androidx.compose.ui.text.TextStyle(
                color = textColor.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            
            Column(horizontalAlignment = Alignment.Start) {
                Text("NOW", style = labelStyle)
                Text("${temps[0].roundToInt()}°", color = textColor, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MAX", style = labelStyle, color = Color(0xFFFF7043).copy(alpha = 0.7f))
                Text("${maxTemp.roundToInt()}°", color = textColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MIN", style = labelStyle, color = Color(0xFF81D4FA).copy(alpha = 0.7f))
                Text("${minTemp.roundToInt()}°", color = textColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${dataPoints.last().time.take(2)}h", style = labelStyle)
                Text("${temps.last().roundToInt()}°", color = textColor, fontWeight = FontWeight.Medium, fontSize = 11.sp)
            }
        }
    }
}
