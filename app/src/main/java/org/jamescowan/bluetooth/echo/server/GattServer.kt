package org.jamescowan.bluetooth.echo.service

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import org.jamescowan.bluetooth.echo.Constants
import org.jamescowan.bluetooth.echo.packet.Packet
import org.jamescowan.bluetooth.echo.server.IGattServer
import org.jamescowan.bluetooth.echo.server.IGattServerListener
import timber.log.Timber
import java.util.*


abstract class GattServer(context:Context) : IGattServer {

    private val advertiseCallback: GattAdvertiseCallback = GattAdvertiseCallback()
    private var bluetoothGattServer: BluetoothGattServer
    private var bluetoothManager:BluetoothManager
    protected val context:Context
    private var listener: IGattServerListener

    init {
        this.context = context
        this.listener = getListener()
        this.bluetoothManager = getBluetoothManager()
        this.bluetoothGattServer = createServer()
    }

    override fun close() {
        this.stopAdvertising()
        this.bluetoothGattServer.close()
    }

    override fun connect(device: BluetoothDevice) {
    }

    override fun startAdvertising() {
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
        val bluetoothLeAdvertiser: BluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build()

        val parcelUuid = ParcelUuid(getServiceUUID())
        val data = AdvertiseData.Builder()
                //.setIncludeDeviceName(true)
                .addServiceUuid(parcelUuid)
                .build()

        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback)
    }

    override fun stopAdvertising() {
        val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
        val bluetoothLeAdvertiser: BluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
    }

    override fun write(message: String): Boolean {
        return true;
    }

    abstract fun getListener(): IGattServerListener
    abstract fun getServices(): List<BluetoothGattService>
    abstract fun getServiceUUID(): UUID

    private fun createServer():BluetoothGattServer {
        val server:BluetoothGattServer = bluetoothManager.openGattServer(context, GattServerCallback())
        val services:List<BluetoothGattService> = getServices()

        for (service in services) {
            server.addService(service)
        }

        return server;
    }

    private fun getBluetoothManager():BluetoothManager {
        return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private inner class GattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {

            super.onConnectionStateChange(device, status, newState)

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    listener.addDevice(device)
                    Timber.i("BluetoothDevice connected: ${device.address} bonded: ${device.bondState}")

                    val isConnected = this@GattServer.bluetoothGattServer.connect(device, false)

                    Timber.i("BluetoothDevice connection: ${device.address} connected: ${isConnected}")
                }

                BluetoothProfile.STATE_DISCONNECTING -> {
                    Timber.i("BluetoothDevice disconnecting: ${device.address} status: ${status}")
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    listener.removeDevice(device)
                    listener.notify("BluetoothDevice disconnected: ${device.address} status: ${status}")
                    Timber.i("BluetoothDevice disconnected: ${device.address} status: ${status}")
                }
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                                 characteristic: BluetoothGattCharacteristic) {

            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            Timber.i("BluetoothDevice read request: ${device.address}")
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {

            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)

            if(!preparedWrite && responseNeeded) {
                val packet:Packet = Packet.createPacket(value)
                val response:Packet = listener.notifyWrite(packet)

                this@GattServer.bluetoothGattServer.sendResponse(device, requestId, 0, 0, response.data);
            }
            Timber.i("BluetoothDevice write request: ${device.address}")
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice,
                                              requestId: Int, descriptor: BluetoothGattDescriptor,
                                              preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {

            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);

            if (responseNeeded) {
                try {
                    this@GattServer.bluetoothGattServer.sendResponse(device, requestId, GATT_SUCCESS, 0, null);
                    Timber.i("BluetoothDevice descriptor write request: ${device.address}")
                }
                catch (e:Throwable) {
                    Timber.e("Exception "+e.message)
                    e.printStackTrace()
                }
            }
        }

        override fun onNotificationSent(device: BluetoothDevice, status: Int) {

            super.onNotificationSent(device, status)

            Timber.i("GattServer", "onNotificationSent")
        }

        override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {

            super.onExecuteWrite(device, requestId, execute)

            Timber.i("GattServer", "Our gatt server on execute write.")
        }
    }

    private inner class GattAdvertiseCallback : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            listener.notifyAdvertiseSuccess()
            Timber.i("Peripheral advertising started...")
        }

        override fun onStartFailure(errorCode: Int) {
            listener.notifyAdvertiseFailure(errorCode)
            Timber.i("Peripheral advertising failed: $errorCode")
        }
    }
}
