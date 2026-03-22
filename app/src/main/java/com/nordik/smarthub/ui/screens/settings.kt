package com.nordik.smarthub.ui.screens

import com.nordik.smarthub.R
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordik.smarthub.ui.theme.ThemeMode
import com.nordik.smarthub.ui.theme.ThemePreference

@Composable
fun SettingsScreen(
    context: Context,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.weight(1f))
        listOf(
            ThemeMode.SYSTEM to stringResource(R.string.theme_system),
            ThemeMode.LIGHT to stringResource(R.string.theme_light),
            ThemeMode.DARK to stringResource(R.string.theme_dark)
        ).forEach { (mode, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onThemeChange(mode)
                    ThemePreference.save(context, mode)
                }
            ) {
                RadioButton(
                    selected = themeMode == mode,
                    onClick = {
                        onThemeChange(mode)
                        ThemePreference.save(context, mode)
                    }
                )
                Text(label)
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}