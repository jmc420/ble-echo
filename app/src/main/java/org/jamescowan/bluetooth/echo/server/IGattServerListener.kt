package org.jamescowan.bluetooth.echo.server

import android.bluetooth.BluetoothDevice
import org.jamescowan.bluetooth.echo.packet.Packet

interface IGattServerListener {
    fun addDevice(device:BluetoothDevice)
    fun notify(message:String)
    fun notifyAdvertiseFailure(errorCode: Int)
    fun notifyAdvertiseSuccess()
    fun notifyWrite(packet: Packet):Packet
    fun removeDevice(device:BluetoothDevice)
}