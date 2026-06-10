package com.example.dailypulse.clock.enhanced

import androidx.compose.ui.graphics.Color

enum class ClockMode {
    BINARY, // Pure binary (1 column per H/M/S)
    BCD     // Binary Coded Decimal (2 columns per H/M/S)
}

data class BinaryClockTheme(
    val litColor: Color = Color(0xFFFFFFFF),
    val dimColor: Color = Color(0xFFFFFFFF).copy(alpha = 0.08f),
    val accentColor: Color = Color(0xFFE8722A),
    val glowAlpha: Float = 0.12f,
    val dotCornerRadiusRatio: Float = 0.28f
)

data class ClockConfig(
    val mode: ClockMode = ClockMode.BINARY,
    val showSeconds: Boolean = true,
    val theme: BinaryClockTheme = BinaryClockTheme()
)
