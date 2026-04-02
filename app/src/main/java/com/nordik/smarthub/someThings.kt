package com.nordik.smarthub

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun adjustColorByReference(color1: Color, color2: Color): Color {
    // вычисляю яркость цвета 0 = черный, 1 = белый
    val brightness2 = color2.luminance()

    val factor = 1f - brightness2 // Инверсирую яркость цвета
    val r = (color1.red * factor).coerceIn(0f, 1f)
    val g = (color1.green * factor).coerceIn(0f, 1f)
    val b = (color1.blue * factor).coerceIn(0f, 1f)

    return Color(r, g, b)
}