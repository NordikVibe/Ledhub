package com.nordik.smarthub

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

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

    val bleEvents = MutableSharedFlow<BleEvent>(replay = 1, extraBufferCapacity = 5)
    private val _ledCount = MutableStateFlow(5)
    val ledCountStateFlow: StateFlow<Int> get() = _ledCount
    private val notificationReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_WHITE -> {
                    val ledCount = _ledCount.value
                    CoroutineScope(Dispatchers.IO).launch {
                        dialogCharacteristic?.let {
                            it.value = byteArrayOf(192.toByte(), 255.toByte())
                            bluetoothGatt?.writeCharacteristic(it)
                        }
                        delay(200)
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
        val channel = NotificationChannel(channelId, "BLE Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val whitePendingIntent = PendingIntent.getBroadcast(
            this, 0, Intent(ACTION_WHITE).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val offPendingIntent = PendingIntent.getBroadcast(
            this, 1, Intent(ACTION_OFF).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notif_title))
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .addAction(0, getString(R.string.notif_white), whitePendingIntent)
            .addAction(0, getString(R.string.notif_off), offPendingIntent)
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