package com.nordik.smarthub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import yuku.ambilwarna.AmbilWarnaDialog

import com.nordik.smarthub.adjustColorByReference

@Composable
fun HomeScreen(
    context: android.content.Context,
    ledCountState: Int,
    ledColorsState: androidx.compose.runtime.snapshots.SnapshotStateList<Color>,
    brightness: androidx.compose.runtime.MutableState<Float>,
    fillColor: Color,
    onFillColorChange: (Color) -> Unit,
    onLedColorChange: (Int, Color) -> Unit,
    onBrightnessChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        val ledRowsCount = (ledCountState + 9) / 10
        for (rowIndex in 0 until ledRowsCount) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (columnIndex in 0 until 10) {
                    val ledIndex = rowIndex * 10 + columnIndex
                    if (ledIndex < ledColorsState.size) {
                        val currentLedColor = ledColorsState[ledIndex]
                        Box(
                            modifier = Modifier
                                .size(25.dp)
                                .background(currentLedColor, shape = RoundedCornerShape(50))
                                .border(
                                    width = 1.dp,
                                    color = adjustColorByReference(MaterialTheme.colorScheme.primary, currentLedColor),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable {
                                    val colorPickerDialog = AmbilWarnaDialog(
                                        context,
                                        android.graphics.Color.rgb(
                                            (currentLedColor.red * 255).toInt(),
                                            (currentLedColor.green * 255).toInt(),
                                            (currentLedColor.blue * 255).toInt()
                                        ),
                                        object : AmbilWarnaDialog.OnAmbilWarnaListener {
                                            override fun onCancel(dialog: AmbilWarnaDialog?) {}
                                            override fun onOk(
                                                dialog: AmbilWarnaDialog?,
                                                selectedColor: Int
                                            ) {
                                                onLedColorChange(
                                                    ledIndex,
                                                    Color(
                                                        red = ((selectedColor shr 16) and 0xFF) / 255f,
                                                        green = ((selectedColor shr 8) and 0xFF) / 255f,
                                                        blue = (selectedColor and 0xFF) / 255f
                                                    )
                                                )
                                            }
                                        }
                                    )
                                    colorPickerDialog.show()
                                }
                        )
                    } else {
                        Box(modifier = Modifier.size(25.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Slider(
                value = brightness.value,
                onValueChange = {
                    if (brightness.value.toInt() != it.toInt()) {
                        brightness.value = it
                        onBrightnessChange(it)
                    }
                },
                valueRange = 0f..255f,
                steps = 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Text("${(brightness.value / 255f * 100f).toInt()}%", textAlign = TextAlign.Center)
        }

        Button(onClick = {
            val dialog = AmbilWarnaDialog(
                context,
                android.graphics.Color.rgb(
                    (fillColor.red * 255).toInt(),
                    (fillColor.green * 255).toInt(),
                    (fillColor.blue * 255).toInt()
                ),
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {}
                    override fun onOk(dialog: AmbilWarnaDialog?, selectedColor: Int) {
                        val chosen = Color(
                            red = ((selectedColor shr 16) and 0xFF) / 255f,
                            green = ((selectedColor shr 8) and 0xFF) / 255f,
                            blue = (selectedColor and 0xFF) / 255f
                        )
                        onFillColorChange(chosen)
                    }
                }
            )
            dialog.show()
        }) {
            Icon(Icons.Default.Create, "")
        }
    }
}