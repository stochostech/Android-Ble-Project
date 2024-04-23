package com.stochostech.kotlinble.models

import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid

data class Peripherals(
    val peripheralMacAddress: BluetoothDevice,
    val peripheralName: String,
    val peripheralServiceUuid: ParcelUuid
)