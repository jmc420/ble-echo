package org.jamescowan.bluetooth.echo.scanner

import java.util.UUID

interface IScanner {
    fun startScan(serviceUUID: UUID)
    fun stopScan()
}