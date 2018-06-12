package org.jamescowan.bluetooth.echo.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import org.jamescowan.bluetooth.echo.Constants
import org.jamescowan.bluetooth.echo.packet.Packet
import org.jamescowan.bluetooth.echo.server.IGattServerListener
import timber.log.Timber
import java.util.*

class GattEchoServer(context:Context) : GattServer(context), IGattServerListener {

    init {
        startAdvertising()
    }

    // abstract methods overrides

    override fun getServices(): List<BluetoothGattService> {
        val result: MutableList<BluetoothGattService> = mutableListOf()
        val service = BluetoothGattService(getServiceUUID(), BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic: BluetoothGattCharacteristic = BluetoothGattCharacteristic(
                Constants.CharacteresticUUID(),
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ)
        val descriptor = BluetoothGattDescriptor(Constants.DescriptorUUID(),
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)

        characteristic.addDescriptor(descriptor)
        service.addCharacteristic(characteristic)
        result.add(service)

        return result;
    }

    override fun getListener(): IGattServerListener {
        return this
    }

    override fun getServiceUUID(): UUID {
        return Constants.ServiceUUID()
    }

    override fun notifyWrite(packet: Packet): Packet {
        Timber.i("Received "+String(packet.message))
        return packet
    }

    // IGattServerListener overrides

    override fun addDevice(device: BluetoothDevice) {

    }
    override fun notifyAdvertiseFailure(errorCode: Int) {

    }

    override fun notifyAdvertiseSuccess() {

    }

    override fun removeDevice(device: BluetoothDevice) {

    }


}