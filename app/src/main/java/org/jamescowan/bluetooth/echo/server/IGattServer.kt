package org.jamescowan.bluetooth.echo.server

import android.bluetooth.BluetoothDevice
import android.content.Context

interface IGattServer {
    fun close()
    fun connect(device: BluetoothDevice)
    fun startAdvertising()
    fun stopAdvertising()
    fun write(message:String):Boolean
}

