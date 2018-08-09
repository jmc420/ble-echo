package org.jamescowan.bluetooth.echo.client

import android.bluetooth.*
import android.content.Context
import android.widget.Toast
import org.jamescowan.bluetooth.echo.Constants
import org.jamescowan.bluetooth.echo.packet.Packet
import timber.log.Timber
import java.util.*

class GattClient(serviceUUID: UUID, characteristicUUID: UUID, listener: IGattClientListener) : IGattClient {

    private var characteristicUUID: UUID
    private lateinit var connection: BluetoothGatt
    private var connected = false;
    private var listener: IGattClientListener
    private var packets: MutableList<Packet> = mutableListOf()
    private var serviceUUID: UUID

    private val MAXBYTES = 19

    init {
        this.serviceUUID = serviceUUID
        this.characteristicUUID = characteristicUUID
        this.listener = listener
    }

    override public fun close() {
        if (connected) {
            connection.disconnect()
            connected = false;
        }
    }

    override public fun connect(context: Context, device: BluetoothDevice) {
        connection = device.connectGatt(context, false, GattCallback())
    }

    override public fun write(message: String): Boolean {

        if (!connected) {
            return false;
        }

        this.packets = createPackets(message)

        var characteristic:BluetoothGattCharacteristic? = findCharacteristic(connection)

        if (characteristic != null) {
            return write(characteristic)
        }

        Timber.e("Cannot find characteristic "+characteristicUUID)

        return true;
    }

    private fun copyBytes(prefix:Byte, source:ByteArray):ByteArray {
        val result:ByteArray = ByteArray(source.size+1)

        result.set(0,prefix)

        var count:Int = 0;
        val max:Int = source.size

        while (count < max){
            result.set(count+1, source[count++])
        }

        return result
    }

    private fun createPackets(message:String):MutableList<Packet> {
        val result: MutableList<Packet> = mutableListOf()
        var data:String = message

        while (data.length > MAXBYTES) {
            val fragment:String = data.substring(0, MAXBYTES)
            val bytes:ByteArray = fragment.toByteArray(Charsets.UTF_8)

            result.add(Packet(false, bytes))
            data = data.substring(19)
        }

        result.add(Packet(true, data.toByteArray(Charsets.UTF_8)))

        return result
    }

    private fun disconnect(gatt: BluetoothGatt) {
        gatt.disconnect()
        connected = false;
        listener.isClosed()
    }

    private fun findCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
        if (gatt.getService(serviceUUID) == null) {
            return null
        }
        return gatt.getService(serviceUUID).getCharacteristic(characteristicUUID)
    }

    private fun write(characteristic: BluetoothGattCharacteristic):Boolean {

        if (packets.size > 0) {
            val packet:Packet = packets.removeAt(0)

            Timber.i("Write "+String(packet.message).toString())

            characteristic.setValue(packet.data)

            if (!connection.writeCharacteristic(characteristic)) {
                Timber.e("Write error");
                return false;
            }
            return true
        }

        return false;
    }

    private inner class GattCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState);

            when {
                newState == BluetoothProfile.STATE_DISCONNECTED -> {
                    listener.isClosed()
                    listener.notify("Disconnected " + gatt.device.address+ " status: "+status)
                    gatt.close()
                    Timber.i("Disconnected " + gatt.device.address+ " status: "+status)
                }
                newState == BluetoothProfile.STATE_CONNECTED -> {
                    Timber.i("Connected " + gatt.device.address)
                    gatt.discoverServices();
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            val value:ByteArray = characteristic.value
            val message:String = String(bytes=value,offset=1, length=value.size-1)

            Timber.i("Changed read " + message)

            listener.packetReceived(Packet.createPacket(value))
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Timber.i("Read");
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Timber.i("onCharacteristicWrite  "+String(characteristic.value))
                listener.packetReceived(Packet.createPacket(characteristic.value))
                if (characteristic.uuid.equals(characteristicUUID)) {
                    write(characteristic)
                }
                else {
                    Timber.i("Unknown uuid "+characteristic.uuid)
                }
            }
            else {
                Timber.e("onCharacteristicWrite error "+status)
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor,
                                       status: Int) {
            Timber.i("onDescriptorWrite ")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Timber.e("Device service discovery unsuccessful, status $status")
                disconnect(gatt)
                return
            }

            var characteristic: BluetoothGattCharacteristic? = findCharacteristic(gatt)

            if (characteristic == null) {
                Timber.e("Unable to find echo characteristic.")
                disconnect(gatt);
                return;
            }

            val characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true)

            if (!characteristicWriteSuccess) {
                Timber.e("Cannot initialise characteristic")
                return
            }

            for (descriptor in characteristic.descriptors) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }

            Timber.i("Characteristic initialised")
            this@GattClient.connected = true;
            listener.isConnected()
        }
    }

}