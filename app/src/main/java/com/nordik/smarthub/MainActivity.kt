package com.nordik.smarthub

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service



import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build

import android.os.Bundle
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// ...existing imports...
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import yuku.ambilwarna.AmbilWarnaDialog

import java.util.*

class LedTileService : TileService() {

    override fun onClick() {
        val intent = Intent(this, BleService::class.java)
        startForegroundService(intent)

        val tile = qsTile
        if (tile.state == Tile.STATE_ACTIVE) {
            // Выключить — отправить чёрный
            sendBroadcast(Intent(BleService.ACTION_OFF).setPackage(packageName))
            tile.state = Tile.STATE_INACTIVE
        } else {
            // Включить — отправить белый
            sendBroadcast(Intent(BleService.ACTION_WHITE).setPackage(packageName))
            tile.state = Tile.STATE_ACTIVE
        }
        tile.updateTile()
    }
}
class BleService : Service() {
    var bluetoothGatt: BluetoothGatt? = null
    var ledColorStreamCharacteristic: BluetoothGattCharacteristic? = null
    var dialogCharacteristic: BluetoothGattCharacteristic? = null

    companion object {
        const val ACTION_WHITE = "com.nordik.smarthub.ACTION_WHITE"
        const val ACTION_OFF = "com.nordik.smarthub.ACTION_OFF"
    }
    sealed class BleEvent {
        data class LedCount(val count: Int): BleEvent()
        data class ColorsReady(val characteristic: BluetoothGattCharacteristic): BleEvent()
        data class DialogReady(val characteristic: BluetoothGattCharacteristic): BleEvent()
        data class Connected(val gatt: BluetoothGatt): BleEvent()
        data class Disconnected(val gatt: BluetoothGatt): BleEvent()
    }
    private val LED_SERVICE_UUID = UUID.fromString("99b0bf79-fe33-4fa4-9e1c-263398667c40")
    private val LED_COLOR_STREAM_UUID = UUID.fromString("2c0a8901-383b-4b83-a88b-6e024a71bc22")
    private val LED_COUNT_UUID = UUID.fromString("337597f2-02e7-4cc6-938b-e0125160161b")
    private val DIALOG_UUID = UUID.fromString("a7fed865-d364-424c-87d6-9e893fb661c4")

    // make replay = 1 so late subscribers receive the last event (e.g. LedCount)
    val bleEvents = MutableSharedFlow<BleEvent>(replay = 1, extraBufferCapacity = 5)
    private val _ledCount = MutableStateFlow(5) // начальное значение 29
    val ledCountStateFlow: StateFlow<Int> get() = _ledCount
    private val notificationReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_WHITE -> {
                    val ledCount = _ledCount.value
                    CoroutineScope(Dispatchers.IO).launch {
                        // Сначала яркость
                        dialogCharacteristic?.let {
                            it.value = byteArrayOf(192.toByte(), 255.toByte())
                            bluetoothGatt?.writeCharacteristic(it)
                        }
                        // Ждём onCharacteristicWrite через suspendCoroutine
                        delay(200) // простое решение — подождать достаточно
                        // Потом цвета
                        ledColorStreamCharacteristic?.let {
                            it.value = ByteArray(ledCount * 3) { 255.toByte() }
                            bluetoothGatt?.writeCharacteristic(it)
                        }
                    }
                }
                ACTION_OFF -> {
                    ledColorStreamCharacteristic?.let {
                        it.value = ByteArray(_ledCount.value * 3) { 0 }
                        bluetoothGatt?.writeCharacteristic(it)
                    }
                }
            }
        }
    }

    // В onCreate сервиса зарегистрируй receiver
    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(ACTION_WHITE)
            addAction(ACTION_OFF)
        }
        registerReceiver(notificationReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        Log.d("BLE", "BleService onDestroy called")
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }
    fun createNotification(): Notification {
        val channelId = "ble_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "BLE Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val whitePendingIntent = PendingIntent.getBroadcast(
            this, 0, Intent(ACTION_WHITE).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val offPendingIntent = PendingIntent.getBroadcast(
            this, 1, Intent(ACTION_OFF).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smart LED подключен")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .addAction(0, "⬜ Белый", whitePendingIntent)
            .addAction(0, "⬛ Выкл", offPendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        connectBLE()
        return START_STICKY
    }
    inner class LocalBinder : Binder() {
        fun getService(): BleService = this@BleService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return LocalBinder()
    }
    val bleGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("BLE", "onConnectionStateChange status=$status newState=$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Connected! Starting service discovery...")
                gatt?.let { bleEvents.tryEmit(BleEvent.Connected(it)) }
                gatt?.discoverServices()  // ← вот это главное
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLE", "Disconnected")
                gatt?.let { bleEvents.tryEmit(BleEvent.Disconnected(it)) }
            }
        }
        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val ledCountChar = gatt?.getService(LED_SERVICE_UUID)?.getCharacteristic(LED_COUNT_UUID)
                ledCountChar?.let {
                    gatt.readCharacteristic(it) // Асинхронно
                }

                val colorChar = gatt?.getService(LED_SERVICE_UUID)?.getCharacteristic(LED_COLOR_STREAM_UUID)
                val dialogChar = gatt?.getService(LED_SERVICE_UUID)?.getCharacteristic(DIALOG_UUID)

                ledColorStreamCharacteristic = colorChar
                dialogCharacteristic = dialogChar

                colorChar?.let { bleEvents.tryEmit(BleEvent.ColorsReady(it)) }
                dialogChar?.let { bleEvents.tryEmit(BleEvent.DialogReady(it)) }

                gatt?.setCharacteristicNotification(colorChar, true)
                gatt?.setCharacteristicNotification(dialogChar, true)

                Log.d("BLE", "onServicesDiscovered status=$status")
                val service = gatt?.getService(LED_SERVICE_UUID)
                Log.d("BLE", "LED service found: $service")
                Log.d("BLE", "ledCountChar found: $ledCountChar")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic?.uuid == LED_COUNT_UUID) {
                val count = characteristic.value[0].toInt() and 0xFF
                _ledCount.value = count
                // tryEmit so we don't suspend in a GATT callback thread and so
                // late collectors can receive if replay is enabled
                Log.d("BleService", "read led count=$count, emitting event")
                bleEvents.tryEmit(BleEvent.LedCount(count))
            }
            Log.d("BLE", "onCharacteristicRead uuid=${characteristic?.uuid} status=$status value=${characteristic?.value?.toList()}")

        }
    }
    @SuppressLint("MissingPermission")
    fun connectBLE() {
        if (bluetoothGatt != null) {
            Log.d("BLE", "already connected, skipping")
            return
        }
        Log.d("BLE", "connectBLE called")
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val remoteDevice = bluetoothAdapter.getRemoteDevice("E8:6B:EA:D4:68:3A")
        Log.d("BLE", "connecting to $remoteDevice")
        bluetoothGatt = remoteDevice?.connectGatt(this, true, bleGattCallback)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private var bleService: BleService? = null
    private val bleEvents = MutableSharedFlow<BleService.BleEvent>(extraBufferCapacity = 5)


    // Bridge state shared between service collectors and Compose UI.
    // Создаём здесь ненулевое состояние, чтобы подписчики сервиса могли
    // сразу записывать значение, даже если композиция ещё не инициализирована.
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
            // push current value into UI bridge immediately in case service
            // already read the characteristic before the UI subscribed
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

    override fun onDestroy() {
        Log.d("BLE", "onDestroy called")
        super.onDestroy()
        unbindService(serviceConnection)
        // убрать unbindService отсюда
    }
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
                        ledColors.getOrElse(ledIndex) { Color.Black } // если меньше, чем ledCount
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
            val selectedTab = remember { mutableStateOf(0) }
            val bleServiceState by _bleService.collectAsState()  // ← вот отсюда берётся bleServiceState
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
                                val gatt = bleService?.bluetoothGatt ?: bluetoothGatt
                                if (brightness.value.toInt() != it.toInt()){
                                    brightness.value = it
                                    dialogCharacteristic?.let { targetCharacteristic ->
                                        val payloadBytes = ByteArray(2)
                                        payloadBytes[0] = 192.toByte()
                                        payloadBytes[1] = brightness.value.toInt().coerceIn(0, 255).toByte()
                                        Log.d("Send Bytes", payloadBytes.joinToString(" ") { "%02X".format(it) })
                                        targetCharacteristic.value = payloadBytes
                                        gatt?.writeCharacteristic(targetCharacteristic)
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

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("LED Controller") })
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

