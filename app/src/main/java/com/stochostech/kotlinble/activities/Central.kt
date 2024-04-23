package com.stochostech.kotlinble.activities

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.stochostech.kotlinble.PermissionsHelper
import com.github.stochostech.ble.Client
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.stochostech.kotlinble.adapters.PeripheralsAdapter
import com.stochostech.kotlinble.databinding.ActivityCentralBinding
import com.stochostech.kotlinble.models.Peripherals
import kotlinx.coroutines.launch

class Central : AppCompatActivity() {

    private lateinit var binding: ActivityCentralBinding

    private lateinit var peripheralsAdapter: PeripheralsAdapter
    private lateinit var bleClient: Client
    private lateinit var peripheralsList: ArrayList<Peripherals>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCentralBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()

        bleClient = Client(this, this)

        val permissionsHelper = PermissionsHelper(this)

        val multiplePermissionsListener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (!report.areAllPermissionsGranted()) {
                        Toast.makeText(
                            this@Central,
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
            permissionsHelper.checkClientCoreBluetoothPermissions(multiplePermissionsListener)
        }

        binding.apply {
            scanBTN.setOnClickListener {
                try {
                    checkPermissions()
                } catch (e: Exception) {
                    Toast.makeText(this@Central, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            disconnectBTN.setOnClickListener {
                try {
                    bleClient.disconnectFromPeripheral()
                } catch (e: Exception) {
                    Toast.makeText(this@Central, e.message, Toast.LENGTH_SHORT).show()
                }
                binding.receivedTV.text = ""
            }

            stopScanBTN.setOnClickListener {
                try {
                    bleClient.stopScan()
                } catch (e: Exception) {
                    Toast.makeText(this@Central, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            bleClient.getTransactions()
        }

        displayScannedDevices()
        displayCharacteristicsValue()
        updateConnectedDevices()
    }

    private fun setUpRecyclerView() {
        peripheralsList = ArrayList()
        peripheralsAdapter = PeripheralsAdapter(peripheralsList)
        binding.rv1.apply {
            adapter = peripheralsAdapter
            layoutManager = LinearLayoutManager(this@Central)
        }

        peripheralsAdapter.setPeripheralClickListener { peripheral ->
            bleClient.connectToPeripheral(peripheral.peripheralMacAddress)
        }
    }

    private fun displayScannedDevices() {
        bleClient.scannedDevices.observe(this) { scanResult ->
            if (scanResult.scanRecord!!.deviceName != null && scanResult.scanRecord!!.serviceUuids != null) {
                val peripheral = Peripherals(
                    scanResult.device,
                    scanResult.scanRecord!!.deviceName!!,
                    scanResult.scanRecord!!.serviceUuids[0]
                )
                peripheralsList.clear()
                peripheralsList.add(peripheral)
                peripheralsAdapter.notifyDataSetChanged()

                Log.d("BLE", "displayScanned: $peripheral")
                Log.d("BLE", "displayScannedDevices: $peripheralsList")
            }
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
                            bleClient.scanForDevices()
                        } else {
                            Toast.makeText(
                                this@Central,
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

    private fun displayCharacteristicsValue() {
        bleClient.notifiedCharacteristic.observe(this) { value ->
            binding.receivedTV.text = value
        }
    }

    private fun updateConnectedDevices() {
        bleClient.connectedDevice.observe(this) { value ->
            binding.tv5.text = "Connection Status: $value"
            if (value == "not connected") {
                binding.receivedTV.text = ""
            }
        }
    }

    override fun onDestroy() {
        try {
            bleClient.disconnectFromPeripheral()
//            bleClient.networkConnection.removeObservers(this)
        } catch (e: Exception) {
            Toast.makeText(this@Central, e.message, Toast.LENGTH_SHORT).show()
        }
        super.onDestroy()
    }
}
