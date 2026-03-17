package com.nordik.smarthub

import android.Manifest
import android.annotation.SuppressLint

import android.content.pm.PackageManager

import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.os.Build

import android.os.Bundle
import android.os.Handler
import android.os.Looper

import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// ...existing imports...
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.*

import yuku.ambilwarna.AmbilWarnaDialog

import java.util.*
// ...existing imports...

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private var ledCountStateBridge: MutableState<Int>? = null

    private val LED_SERVICE_UUID = UUID.fromString("99b0bf79-fe33-4fa4-9e1c-263398667c40")
    //private val CHARACTERISTIC_UUID = UUID.fromString("e9a46c93-13f2-42c8-b7fd-4e9221b929f4")
    private val LED_COLOR_STREAM_UUID = UUID.fromString("2c0a8901-383b-4b83-a88b-6e024a71bc22")
    private val LED_COUNT_UUID = UUID.fromString("337597f2-02e7-4cc6-938b-e0125160161b")
    private val DIALOG_UUID = UUID.fromString("a7fed865-d364-424c-87d6-9e893fb661c4")
    private var bluetoothGatt: BluetoothGatt? = null
    private var ledColorStreamCharacteristic: BluetoothGattCharacteristic? = null
    private var dialogCharacteristic: BluetoothGattCharacteristic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    ),
                    1
                )
                return
            }
        }
        val bleGattCallback = object : BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val ledCountCharacteristic =
                        gatt?.getService(LED_SERVICE_UUID)?.getCharacteristic(LED_COUNT_UUID)
                    gatt?.readCharacteristic(ledCountCharacteristic)
                    ledColorStreamCharacteristic = gatt?.getService(LED_SERVICE_UUID)?.getCharacteristic(LED_COLOR_STREAM_UUID)
                    dialogCharacteristic = gatt?.getService(LED_SERVICE_UUID)?.getCharacteristic(DIALOG_UUID)
                    gatt?.setCharacteristicNotification(ledColorStreamCharacteristic, true)
                    gatt?.setCharacteristicNotification(dialogCharacteristic, true)
                }
            }

            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt?.discoverServices()

                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                if (status != BluetoothGatt.GATT_SUCCESS || characteristic == null) return
                if (characteristic.uuid != LED_COUNT_UUID) return

                val rawValueBytes = characteristic.value
                val reportedLedCount = rawValueBytes?.get(0)?.toInt()?.and(0xFF) ?: 29

                Log.d("BLE", "LED size = " + rawValueBytes.joinToString(" ") { "%02X".format(it) })

                runOnUiThread {
                    ledCountStateBridge?.value = reportedLedCount
                }
            }
        }
        fun isGattConnected(gatt: BluetoothGatt?): Boolean {
            return try {
                gatt?.let { it.device.bondState == BluetoothDevice.BOND_BONDED || it.services.isNotEmpty() } ?: false
            } catch (e: Exception) {
                false
            }
        }
        fun connectBleWithRetry(callback: BluetoothGattCallback, maxRetries: Int = 5) {
            var attempt = 0
            fun tryConnect() {
                attempt++
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val device = bluetoothAdapter.getRemoteDevice("E8:6B:EA:D4:68:3A")
                bluetoothGatt = device?.connectGatt(this, false, callback)

                // Таймаут для соединения
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000) // ждём 3 секунды
                    if (bluetoothGatt == null || !isGattConnected(bluetoothGatt)) {
                        bluetoothGatt?.close()
                        bluetoothGatt = null
                        if (attempt < maxRetries) {
                            Log.d("BLE", "Retrying connection, attempt $attempt")
                            tryConnect()
                        } else {
                            Log.d("BLE", "Max retries reached")
                        }
                    }
                }
            }
            tryConnect()
        }
        fun reconnectBle() {
            bluetoothGatt?.let { gatt ->
                try {
                    gatt.disconnect()
                    gatt.close()
                } catch (e: Exception) {
                    Log.e("BLE", "Error closing old GATT: ${e.message}")
                }
            }
            bluetoothGatt = null
            ledColorStreamCharacteristic = null

            // Немного ждём после перезагрузки ESP
            CoroutineScope(Dispatchers.Main).launch {
                delay(700) // 0.7 секунды, можно подкорректировать
                connectBleWithRetry(bleGattCallback)
            }
            ledColorStreamCharacteristic = bluetoothGatt?.getService(LED_SERVICE_UUID)?.getCharacteristic(LED_COLOR_STREAM_UUID)
            dialogCharacteristic = bluetoothGatt?.getService(LED_SERVICE_UUID)?.getCharacteristic(DIALOG_UUID)
        }

        fun connectBLE(bleGattCallback: BluetoothGattCallback) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val remoteDevice: BluetoothDevice? =
                bluetoothAdapter.getRemoteDevice("E8:6B:EA:D4:68:3A")
            bluetoothGatt = remoteDevice?.connectGatt(this, true, bleGattCallback)
        }
        fun reConnectBLE() {
            bluetoothGatt?.let {
                it.disconnect()
                it.close()
            }
            bluetoothGatt = null
            Handler(Looper.getMainLooper()).postDelayed({
                connectBLE(bleGattCallback)
            }, 500)
        }

        fun sendLedColorsPayload(ledColors: List<Color>, ledCount: Int) {
            ledColorStreamCharacteristic?.let { targetCharacteristic ->
                val payloadBytes =
                    ByteArray(ledCount * 3) // строго столько, сколько нужно светодиодов
                for (ledIndex in 0 until ledCount) {
                    val ledColor =
                        ledColors.getOrElse(ledIndex) { Color.Black } // если меньше, чем ledCount
                    val baseByteIndex = ledIndex * 3
                    payloadBytes[baseByteIndex + 0] =
                        (ledColor.red * 255).toInt().coerceIn(0, 255).toByte()
                    payloadBytes[baseByteIndex + 1] =
                        (ledColor.green * 255).toInt().coerceIn(0, 255).toByte()
                    payloadBytes[baseByteIndex + 2] =
                        (ledColor.blue * 255).toInt().coerceIn(0, 255).toByte()
                }
                Log.d("Send Bytes", payloadBytes.joinToString(" ") { "%02X".format(it) })
                targetCharacteristic.value = payloadBytes
                bluetoothGatt?.writeCharacteristic(targetCharacteristic)
            }
        }


        connectBLE(bleGattCallback)
        setContent {
            val selectedTab = remember { mutableStateOf(0) }
            val ledCountState = remember { mutableStateOf(0) }
            ledCountStateBridge = ledCountState

            val ledColorsState = remember {
                mutableStateListOf<Color>().apply {
                    repeat(ledCountState.value) {
                        add(Color.Black)
                    }
                }
            }
            val brightness = remember { mutableStateOf(200F)}
            var fillColor by remember { mutableStateOf(Color.Black) }

            fun fillStrip(color: Color) {
                for (i in ledColorsState.indices) {
                    ledColorsState[i] = color
                }
            }
            @Composable
            fun homeScreen() {
                Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val ledRowsCount = (ledCountState.value + 9) / 10
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
                                                width = (1 / 2).dp,
                                                color = Color.Black,
                                                shape = RoundedCornerShape(50)
                                            )
                                            .clickable {
                                                val colorPickerDialog = AmbilWarnaDialog(
                                                    this@MainActivity,
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
                                                            if (ledIndex < ledColorsState.size) {
                                                                ledColorsState[ledIndex] = Color(
                                                                    red = ((selectedColor shr 16) and 0xFF) / 255f,
                                                                    green = ((selectedColor shr 8) and 0xFF) / 255f,
                                                                    blue = (selectedColor and 0xFF) / 255f
                                                                )
                                                            }
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
                                if (brightness.value.toInt() != it.toInt()){
                                    brightness.value = it
                                    dialogCharacteristic?.let { targetCharacteristic ->
                                        val payloadBytes = ByteArray(2)
                                        payloadBytes[0] = 192.toByte()
                                        payloadBytes[1] = brightness.value.toInt().coerceIn(0, 255).toByte()
                                        Log.d("Send Bytes", payloadBytes.joinToString(" ") { "%02X".format(it) })
                                        targetCharacteristic.value = payloadBytes
                                        bluetoothGatt?.writeCharacteristic(targetCharacteristic)
                                    }
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
                            this@MainActivity,
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
                                    fillColor = chosen
                                    fillStrip(chosen)
                                }
                            }
                        )
                        dialog.show()
                    }) {
                        Icon(Icons.Default.Create, contentDescription = "Fill all LEDs")
                    }
                }
            }

            LaunchedEffect(ledCountState.value) {
                val ledColorSlotsDelta = ledCountState.value - ledColorsState.size
                if (ledColorSlotsDelta > 0) repeat(ledColorSlotsDelta) { ledColorsState.add(Color.Black) }
                else if (ledColorSlotsDelta < 0) repeat(-ledColorSlotsDelta) {
                    ledColorsState.removeAt(
                        ledColorsState.lastIndex
                    )
                }
            }
            LaunchedEffect(ledCountState.value) {
                var currentPayload: List<Color>
                var previousPayload: List<Color> = emptyList()
                while (true) {
                    currentPayload = ledColorsState.take(ledCountState.value)
                    if (previousPayload != currentPayload) {
                        sendLedColorsPayload(currentPayload, ledCountState.value)
                        previousPayload = currentPayload
                    }
                    delay(50)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("LED Controller") },
                        actions = {
                            IconButton(onClick = { reconnectBle() }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "ReConnect"
                                )
                            }
                        })
                },
                bottomBar = {
                    NavigationBar() {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = selectedTab.value == 0,
                            onClick = { selectedTab.value = 0 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Star, contentDescription = "Effects") },
                            label = { Text("Effects") },
                            selected = selectedTab.value == 1,
                            onClick = { selectedTab.value = 1 }
                        )
                    }
                }

            ) { padding ->
                // Дополнительный внешний отступ для всего контента в Scaffold
                Box(Modifier.padding(padding).padding(16.dp)) {
                    when (selectedTab.value) {
                        0 -> homeScreen()
                        1 -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { Text("Effects coming soon!") }
                    }
                }
            }
        }
    }
}

