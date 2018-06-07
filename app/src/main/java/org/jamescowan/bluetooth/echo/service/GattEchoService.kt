package org.jamescowan.bluetooth.echo.service

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.IBinder
import org.jamescowan.bluetooth.echo.server.IGattServerListener
import timber.log.Timber

class GattEchoService : Service() {

    private lateinit var gattEchoServer:GattEchoServer
    // service overrides

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {

        Timber.i("Created service ")

        gattEchoServer = GattEchoServer(this.applicationContext)
    }

    override fun onDestroy() {
        gattEchoServer.close()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Timber.i("onStartCommand")

        return Service.START_STICKY
    }
}