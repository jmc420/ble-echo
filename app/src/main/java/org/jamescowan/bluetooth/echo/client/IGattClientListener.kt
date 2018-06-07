package org.jamescowan.bluetooth.echo.client

interface IGattClientListener {
    fun isConnected()
    fun isClosed()
    fun response(message:String)
}