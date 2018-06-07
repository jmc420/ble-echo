package org.jamescowan.bluetooth.echo.scanner

import android.bluetooth.BluetoothDevice

interface IScannerListener {
    fun foundDevice(device:BluetoothDevice)
}