package org.jamescowan.bluetooth.echo.packet

class Packet(isEnd:Boolean, message:ByteArray) {

    val data:ByteArray
    val isEnd:Boolean
    val message:ByteArray

    init {
        this.isEnd = isEnd
        this.message = message
        if (isEnd) {
            data = copyBytes(END_PACKET, message)
        }
        else {
            data = copyBytes(0, message)
        }
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

    companion object {
        const val END_PACKET:Byte = 1

        fun createPacket(value:ByteArray):Packet {
            val max = value.size-1
            val copy:ByteArray = ByteArray(max)
            var count = 0;

            while (count < max){
                copy.set(count, value[++count])
            }

            if (value.get(0) == Packet.END_PACKET) {
                return Packet(true, copy)
            }

            return Packet(false, copy)
        }
    }

}