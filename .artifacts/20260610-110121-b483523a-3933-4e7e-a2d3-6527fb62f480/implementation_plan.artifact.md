# Implementation Plan - Neon Refinement & Background Expansion

Completely remove glow effects from all clocks while keeping neon colors, add new background atmospheres, and ensure scheduled Night Mode visibility.

## Proposed Changes

### 1. Clock Refinements (Remove ALL Glow)

#### [DigitalClockView.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/clock/DigitalClockView.kt), [PixelClockView.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/clock/PixelClockView.kt), [AnalogClockView.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/clock/AnalogClockView.kt), [BinaryClockView.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/clock/BinaryClockView.kt), [FlipClockView.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/clock/FlipClockView.kt), [NixieClockView.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/clock/NixieClockView.kt), [WordClockView.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/clock/WordClockView.kt)
- Remove all `drawCircle` or `drawLine` calls that create glow/blur layers.
- Maintain vibrant Neon colors (Cyan, Pink, Lime, etc.) for the main elements.
- Ensure `isNeon` only changes the primary color palette and removes glow.

---

### 2. Background Expansion (New Atmospheres)

#### [BackgroundManager.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/background/BackgroundManager.kt)
- Add new `Atmosphere` variants:
    - **NEON_PULSE**: Pink, Purple, Cyan (Cyberpunk style).
    - **FOREST_MIST**: Dark Green, Emerald, Teal (Nature style).
    - **SOLAR_FLARE**: Deep Red, Orange, Gold (Energy style).
    - **FROZEN_TUNDRA**: Ice Blue, Deep Blue, White (Winter style).
    - **VIOLET_DREAM**: Purple, Magenta, Indigo (Abstract style).

---

### 3. Settings UI (Night Mode Fix)

#### [SettingsPanel.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/settings/SettingsPanel.kt)
- Ensure the Night Mode schedule (Start/End hours and Brightness slider) is correctly rendered inside the "Schermo/Display" section.
- Verify that `nightShiftEnabled` correctly triggers the visibility of these controls.

---

## Verification Plan

### Manual Verification
1. **No Glow**: Verify all clocks (especially Digital and Pixel) have NO glow layers even when Neon mode is ON.
2. **Neon Colors**: Confirm colors are still vibrant (Cyan/Pink) in Neon mode.
3. **New Backgrounds**: Test all new Atmospheres; verify smooth animations and correct color rendering.
4. **Night Mode Settings**:
    - Toggle Night Shift in Settings.
    - Confirm Start/End hour sliders and Brightness slider appear and function.
    - Verify scheduled dimming works by setting the range to include current time.
