package org.jamescowan.bluetooth.echo.client

import org.jamescowan.bluetooth.echo.packet.Packet

interface IGattClientListener {
    fun isConnected()
    fun isClosed()
    fun notify(message:String)
    fun packetReceived(packet: Packet)
}