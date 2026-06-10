package com.dedio.dailypulse.clock

/**
 * Enum representing available clock face types for the screensaver.
 * Each entry has an Italian display name for the UI.
 */
enum class ClockType(val displayName: String) {
    FLIP("Flip Clock"),
    NIXIE("Tubo Nixie"),
    ANALOG("Analogico"),
    DIGITAL("Digitale"),
    PIXEL("Pixel"),
    BINARY("Binario"),
    WORD_CLOCK("Orologio a Parole")
}
