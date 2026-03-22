package com.nordik.smarthub

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class LedTileService : TileService() {

    override fun onClick() {
        val intent = Intent(this, BleService::class.java)
        startForegroundService(intent)

        val tile = qsTile
        if (tile.state == Tile.STATE_ACTIVE) {
            sendBroadcast(Intent(BleService.ACTION_OFF).setPackage(packageName))
            tile.state = Tile.STATE_INACTIVE
        } else {
            sendBroadcast(Intent(BleService.ACTION_WHITE).setPackage(packageName))
            tile.state = Tile.STATE_ACTIVE
        }
        tile.updateTile()
    }
}