
This is an example Bluetooth LE (BLE) Echo program written in Kotlin.

There is a problem when the client connects to the server; in the onDescriptorWriteRequest override of GattServer.gt, there is a null pointer exception caused by the call on line 136 when this call is made:

    this@GattServer.bluetoothGattServer.sendResponse(device, requestId, GATT_SUCCESS, 0, value);

The exception is:

06-07 12:27:39.057 12156-12630/org.jamescowan.bluetooth.echo E/GattServer$GattServerCallback: Exception Attempt to invoke virtual method 'int java.lang.Integer.intValue()' on a null object reference
06-07 12:27:39.059 12156-12630/org.jamescowan.bluetooth.echo W/System.err: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.Integer.intValue()' on a null object reference
06-07 12:27:39.062 12156-12630/org.jamescowan.bluetooth.echo W/System.err:     at android.os.Parcel.readException(Parcel.java:1699)
06-07 12:27:39.065 12156-12630/org.jamescowan.bluetooth.echo W/System.err:     at android.os.Parcel.readException(Parcel.java:1646)
06-07 12:27:39.066 12156-12630/org.jamescowan.bluetooth.echo W/System.err:     at android.bluetooth.IBluetoothGatt$Stub$Proxy.sendResponse(IBluetoothGatt.java:1424)
06-07 12:27:39.068 12156-12630/org.jamescowan.bluetooth.echo W/System.err:     at android.bluetooth.BluetoothGattServer.sendResponse(BluetoothGattServer.java:599)
06-07 12:27:39.070 12156-12630/org.jamescowan.bluetooth.echo W/System.err:     at org.jamescowan.bluetooth.echo.service.GattServer$GattServerCallback.onDescriptorWriteRequest(GattServer.kt:136)
06-07 12:27:39.072 12156-12630/org.jamescowan.bluetooth.echo W/System.err:     at android.bluetooth.BluetoothGattServer$1.onDescriptorWriteRequest(BluetoothGattServer.java:261)
06-07 12:27:39.073 12156-12630/org.jamescowan.bluetooth.echo W/System.err:     at android.bluetooth.IBluetoothGattServerCallback$Stub.onTransact(IBluetoothGattServerCallback.java:263)
06-07 12:27:39.074 12156-12630/org.jamescowan.bluetooth.echo W/System.err:     at android.os.Binder.execTransact(Binder.java:573)

If you try and trace down into the android SDK to see where the problem is, the source code does not match the byte code. Changing the targetSdkVersion in build.gradle to match the SDK version in the device does not resolve the issue. The techniques described in this SO article do not fix the problem:

https://stackoverflow.com/questions/39990752/source-code-does-not-match-the-bytecode-when-debugging-on-a-device

