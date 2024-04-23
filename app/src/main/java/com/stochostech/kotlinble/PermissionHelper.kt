package com.stochostech.kotlinble

import android.Manifest
import android.content.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionsHelper(private val context: Context) {

    fun checkServerCoreBluetoothPermissions(listener: MultiplePermissionsListener) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            .withListener(listener)
            .check()
    }

    fun checkClientCoreBluetoothPermissions(listener: MultiplePermissionsListener) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
            .withListener(listener)
            .check()
    }
}
