package com.stochostech.kotlinble.activities

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.stochostech.kotlinble.PermissionsHelper
import com.github.stochostech.ble.Server
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.stochostech.kotlinble.databinding.ActivityServerBinding
import kotlinx.coroutines.launch

class Peripheral : AppCompatActivity() {

    private lateinit var binding: ActivityServerBinding
    private lateinit var bleServer: Server

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bleServer = Server(this, this)
        val permissionsHelper = PermissionsHelper(this)

        val multiplePermissionsListener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (!report.areAllPermissionsGranted()) {
                        Toast.makeText(
                            this@Peripheral,
                            "Please grant all permissions",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }
        }

        if (Build.VERSION.SDK_INT >= 31) {
            permissionsHelper.checkServerCoreBluetoothPermissions(multiplePermissionsListener)
        }

        binding.apply {
            disconnectBTN.setOnClickListener {
                bleServer.stopHandlingIncomingConnections()
                tv4.text = "Connection Status: not connected"
                defaultTV.text = ""
            }

            advertisingSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    try {
                        checkPermissions()
                    } catch (e: Exception) {
                        Toast.makeText(this@Peripheral, e.message, Toast.LENGTH_SHORT).show()
                        advertisingSwitch.isChecked = false
                    }
                } else {
                    lifecycleScope.launch {
                        stopServer()
                        tv6.text = "Start Advertising"
                    }
                }
            }

            sendBTN.setOnClickListener {
                if (senderET.text.trim().isEmpty()) {
                    Toast.makeText(this@Peripheral, "Enter a value", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        bleServer.notifyCharacteristics(senderET.text.trim().toString())
                    } catch (e: Exception) {
                        Toast.makeText(this@Peripheral, e.message, Toast.LENGTH_SHORT).show()
                    }

                    senderET.setText("")
                }
            }
        }

        lifecycleScope.launch {
            bleServer.getTransactions()
        }
        displayDefaultCharacteristic()
    }

    @SuppressLint("MissingPermission")
    private suspend fun startServer() {
        try {
            bleServer.startServer()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        displayConnectedDevices()
        binding.tv6.text = "Stop Advertising"
    }

    @SuppressLint("MissingPermission")
    private suspend fun stopServer() {
        try {
            bleServer.stopServer()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report.let {
                        if (report!!.areAllPermissionsGranted()) {
                            lifecycleScope.launch {
                                startServer()
                            }
                        } else {
                            binding.advertisingSwitch.isChecked = false
                            binding.tv6.text = "Start Advertising"
                            Toast.makeText(
                                this@Peripheral,
                                "Please grant all required permission to use the app",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).withErrorListener { error ->
                Toast.makeText(this, error.name, Toast.LENGTH_SHORT).show()
            }.check()
    }

    private fun displayConnectedDevices() {
        bleServer.getConnectedDeviceStatus.observe(this) { value ->
            binding.tv4.text = value
            if (value == "not connected") {
                binding.defaultTV.text = ""
            }
        }
    }

    private fun displayDefaultCharacteristic() {
        bleServer.getDefaultCharacteristicStatus.observe(this) { value ->
            binding.defaultTV.text = value
        }
    }

}
