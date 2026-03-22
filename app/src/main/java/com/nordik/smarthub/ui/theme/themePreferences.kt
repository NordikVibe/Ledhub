package com.nordik.smarthub.ui.theme

import android.content.Context
import androidx.core.content.edit

enum class ThemeMode { LIGHT, DARK, SYSTEM }

object ThemePreference {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "theme_mode"

    fun save(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_THEME, mode.name) }
    }

    fun load(context: Context): ThemeMode {
        val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, ThemeMode.SYSTEM.name)
        return ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
    }
}