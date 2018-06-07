package org.jamescowan.bluetooth.echo

import java.util.*

class Constants {
    companion object {
        const val BLUETOOTH_ARG = "BLUETOOTH_ARG"
        const val CHARACTERISTIC_UUID = "dd8cfcd4-ace4-4deb-b235-6b35f85b264b"
        const val DESCRIPTOR_UUID = "b673644a-bee0-424d-b875-1ddd112d2e41"
        const val SERVICE_UUID = "108e91fd-9cb0-4994-a41f-10a35f6bfdb5"

        fun CharacteresticUUID():UUID {
            return UUID.fromString(CHARACTERISTIC_UUID)
        }

        fun DescriptorUUID():UUID {
            return UUID.fromString(DESCRIPTOR_UUID)
        }

        fun ServiceUUID():UUID {
            return UUID.fromString(SERVICE_UUID)
        }
    }
}