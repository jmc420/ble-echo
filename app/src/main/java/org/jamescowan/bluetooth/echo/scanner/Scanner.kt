package org.jamescowan.bluetooth.echo.scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import org.jamescowan.bluetooth.echo.BluetoothException
import org.jamescowan.bluetooth.echo.R
import timber.log.Timber
import java.util.*

class Scanner(context:Context, listener:IScannerListener) : IScanner {
    private var bluetoothAdapter: BluetoothAdapter
    private var bluetoothManager: BluetoothManager
    private val devices: MutableList<BluetoothDevice> = mutableListOf()
    private val listener: IScannerListener
    private var scanCallback: BluetoothAdapter.LeScanCallback = ScanCallback()

    init {
        this.listener = listener

        checkPermissions(context);
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = getBluetoothAdapter(context)
    }

    override public fun startScan(serviceUUID: UUID) {
        devices.clear()
        bluetoothAdapter.startLeScan(arrayOf(serviceUUID), this.scanCallback)
    }

    override public fun stopScan() {
        bluetoothAdapter.stopLeScan(this.scanCallback)
    }

    private fun checkPermissions(context:Context) {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            throw BluetoothException(context.getString(R.string.bt_permission_required))
        }
    }

    private fun getBluetoothAdapter(context:Context): BluetoothAdapter {

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw BluetoothException(context.getString(R.string.ble_not_supported))
        }

        val btManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        if (btManager != null) {
            val btAdapter: BluetoothAdapter? = btManager.getAdapter()

            if (btAdapter != null) {
                return btAdapter;
            }
        }

        throw BluetoothException(context.getString(R.string.bt_unavailable))
    }

    private inner class ScanCallback : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
            if (!devices.any { it -> it.address == device.address }) {
                Timber.i("Found device address: " + device.address + " name: " + device.name);
                devices.add(device)
                listener.foundDevice(device)
            }
        }
    }
}