package org.jamescowan.bluetooth.echo.server

import android.bluetooth.BluetoothDevice

interface IGattServerListener {
    fun addDevice(device:BluetoothDevice)
    fun notifyAdvertiseFailure(errorCode: Int)
    fun notifyAdvertiseSuccess()
    fun removeDevice(device:BluetoothDevice)
}