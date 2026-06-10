package com.dedio.dailypulse.clock.enhanced

import java.util.Calendar

data class ClockColumn(
    val label: String,
    val value: Int,
    val bits: Int = 6,
    val colorType: ColorType = ColorType.NORMAL
)

enum class ColorType {
    NORMAL,
    DIMMED,
    ACCENT
}

object ClockLogic {
    fun getColumns(mode: ClockMode, showSeconds: Boolean): List<ClockColumn> {
        val cal = Calendar.getInstance()
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        val s = cal.get(Calendar.SECOND)

        return when (mode) {
            ClockMode.BINARY -> {
                mutableListOf<ClockColumn>().apply {
                    add(ClockColumn("H", h, 6, ColorType.NORMAL))
                    add(ClockColumn("M", m, 6, ColorType.DIMMED))
                    if (showSeconds) add(ClockColumn("S", s, 6, ColorType.ACCENT))
                }
            }
            ClockMode.BCD -> {
                mutableListOf<ClockColumn>().apply {
                    add(ClockColumn("H", h / 10, 4, ColorType.NORMAL))
                    add(ClockColumn("h", h % 10, 4, ColorType.NORMAL))
                    add(ClockColumn("M", m / 10, 4, ColorType.DIMMED))
                    add(ClockColumn("m", m % 10, 4, ColorType.DIMMED))
                    if (showSeconds) {
                        add(ClockColumn("S", s / 10, 4, ColorType.ACCENT))
                        add(ClockColumn("s", s % 10, 4, ColorType.ACCENT))
                    }
                }
            }
        }
    }
}
