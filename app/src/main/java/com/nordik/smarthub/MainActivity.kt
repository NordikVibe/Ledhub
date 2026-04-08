package com.nordik.smarthub

import com.nordik.smarthub.ui.screens.*
import com.nordik.smarthub.ui.theme.ThemeMode
import com.nordik.smarthub.ui.theme.*

import android.Manifest

import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager

import android.os.Bundle
import android.os.IBinder

import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private var bleService: BleService? = null
    private val ledCountStateBridge = mutableStateOf(0)
    private val _bleService = MutableStateFlow<BleService?>(null)
    private var bluetoothGatt: BluetoothGatt? = null
    private var ledColorStreamCharacteristic: BluetoothGattCharacteristic? = null
    private var dialogCharacteristic: BluetoothGattCharacteristic? = null

    private fun subscribeToBleEvents() {
        bleService?.let { service ->
            CoroutineScope(Dispatchers.Main).launch {
                service.bleEvents.collect { event ->
                    when(event) {
                        is BleService.BleEvent.LedCount -> {
                            Log.d("MainActivity", "received LedCount=${event.count}")
                            ledCountStateBridge.value = event.count
                        }
                        is BleService.BleEvent.ColorsReady -> ledColorStreamCharacteristic = event.characteristic
                        is BleService.BleEvent.DialogReady -> dialogCharacteristic = event.characteristic
                        is BleService.BleEvent.Connected -> {
                            bluetoothGatt = event.gatt
                            Log.d("MainActivity", "bluetoothGatt set: ${event.gatt}")
                        }
                        is BleService.BleEvent.Disconnected -> {
                            bluetoothGatt = null
                            ledColorStreamCharacteristic = null
                            dialogCharacteristic = null
                        }
                    }
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            bleService = (binder as? BleService.LocalBinder)?.getService()
            _bleService.value = bleService
            bleService?.let { service ->
                val v = service.ledCountStateFlow.value
                Log.d("MainActivity", "service bound, initial ledCount=$v")
                ledCountStateBridge.value = v
            }
            subscribeToBleEvents()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, BleService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                2
            )
        }
        val intent = Intent(this, BleService::class.java)
        startForegroundService(intent)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)




        fun sendLedColorsPayload(ledColors: List<Color>, ledCount: Int) {
            val gatt = bleService?.bluetoothGatt ?: bluetoothGatt  // ← сначала берём из сервиса
            ledColorStreamCharacteristic?.let { targetCharacteristic ->
                val payloadBytes =
                    ByteArray(ledCount * 3) // строго столько, сколько нужно светодиодов
                for (ledIndex in 0 until ledCount) {
                    val ledColor =
                        ledColors.getOrElse(ledIndex) { Color.Black }
                    val baseByteIndex = ledIndex * 3
                    payloadBytes[baseByteIndex + 0] =
                        (ledColor.red * 255).toInt().coerceIn(0, 255).toByte()
                    payloadBytes[baseByteIndex + 1] =
                        (ledColor.green * 255).toInt().coerceIn(0, 255).toByte()
                    payloadBytes[baseByteIndex + 2] =
                        (ledColor.blue * 255).toInt().coerceIn(0, 255).toByte()}
                targetCharacteristic.value = payloadBytes
                gatt?.writeCharacteristic(targetCharacteristic)
                Log.d("Send Bytes", payloadBytes.joinToString(" ") { "%02X".format(it) })

            }
        }
        setContent {
            var themeMode by remember {
                mutableStateOf(ThemePreference.load(this@MainActivity))
            }
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val selectedTab = remember { mutableStateOf(0) }
            val bleServiceState by _bleService.collectAsState()
            val ledFlow = remember(bleServiceState) {
                bleServiceState?.ledCountStateFlow ?: MutableStateFlow(0)
            }
            val ledCountState by ledFlow.collectAsState()
            val ledColorsState = remember {
                mutableStateListOf<Color>().apply {
                    repeat(ledCountState) {
                        add(Color.Black)
                    }
                }
            }
            val effectsExpanded = remember { mutableStateOf(false) }
            val brightness = remember { mutableStateOf(200F)}
            var fillColor by remember { mutableStateOf(Color.Black) }

            LaunchedEffect(ledCountState) {
                val ledColorSlotsDelta = ledCountState - ledColorsState.size
                if (ledColorSlotsDelta > 0) repeat(ledColorSlotsDelta) { ledColorsState.add(Color.Black) }
                else if (ledColorSlotsDelta < 0) repeat(-ledColorSlotsDelta) {
                    ledColorsState.removeAt(
                        ledColorsState.lastIndex
                    )
                }
            }
            LaunchedEffect(ledCountState) {
                var currentPayload: List<Color>
                var previousPayload: List<Color> = emptyList()
                while (true) {
                    currentPayload = ledColorsState.take(ledCountState)
                    if (previousPayload != currentPayload) {
                        sendLedColorsPayload(currentPayload, ledCountState)
                        previousPayload = currentPayload
                    }
                    delay(50)
                }
            }
            MaterialTheme(
                colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
            ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.title)) })
                },
                bottomBar = {
                    NavigationBar() {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.tab_home)) },
                            label = { Text(stringResource(R.string.tab_home)) },
                            selected = selectedTab.value == 0,
                            onClick = { selectedTab.value = 0 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Star, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_presets)) },
                            selected = selectedTab.value == 1,
                            onClick = { selectedTab.value = 1 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_IDE)) },
                            selected = selectedTab.value == 3,
                            onClick = { selectedTab.value = 3 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.tab_settings)) },
                            label = { Text(stringResource(R.string.tab_settings)) },
                            selected = selectedTab.value == 2,
                            onClick = { selectedTab.value = 2 }
                        )
                    }
                }

            ) { padding ->
                Box(Modifier.padding(padding).padding(16.dp)) {
                    when (selectedTab.value) {
                        0 -> HomeScreen(
                                context = this@MainActivity,
                                ledCountState = ledCountState,
                                ledColorsState = ledColorsState,
                                brightness = brightness,
                                fillColor = fillColor,
                                onFillColorChange = { newColor -> fillColor = newColor; for (i in ledColorsState.indices) { ledColorsState[i] = fillColor } },
                                onLedColorChange = { index, color -> ledColorsState[index] = color },
                                onBrightnessChange = {
                                    val gatt = bleService?.bluetoothGatt
                                    bleService?.dialogCharacteristic?.let { char ->
                                        val payloadBytes = ByteArray(2)
                                        payloadBytes[0] = 192.toByte()
                                        payloadBytes[1] = it.toInt().coerceIn(0, 255).toByte()
                                        Log.d("Send Bytes", payloadBytes.joinToString(" ") { "%02X".format(it) })
                                        char.value = payloadBytes
                                        gatt?.writeCharacteristic(char)
                                    }
                                }
                            )
                        1 -> EffectsExp()
                        2 -> SettingsScreen(
                            context = this@MainActivity,
                            themeMode = themeMode,
                            onThemeChange = { themeMode = it}
                        )
                        3 -> EffectsEdit()
                    }
                }
            }
        }
        }
    }
}

