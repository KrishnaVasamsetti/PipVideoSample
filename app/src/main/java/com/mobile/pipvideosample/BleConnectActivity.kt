package com.mobile.pipvideosample

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.mobile.pipvideosample.databinding.ActivityBleConnectBinding
import java.util.*


class BleConnectActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var binding: ActivityBleConnectBinding

    private val pairedDeviceAdapter = PairedDevicesAdapter() {
        connectToDevice(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble_connect)

        bluetoothAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            bluetoothManager.adapter
        } else {
            BluetoothAdapter.getDefaultAdapter()
        }
        if (bluetoothAdapter == null) {
            // Bluetooth is not available
            Toast.makeText(
                applicationContext,
                "Bluetooth Not supported for this device",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

//        enableBluetooth()

        binding.tvSearchForDevices.setOnClickListener {
            startDiscovery()
        }

        binding.rvDevices.adapter = pairedDeviceAdapter

    }

    override fun onResume() {
        super.onResume()
        enableBluetooth()
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // No BLUETOOTH_CONNECT Permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        REQUEST_BLUETOOTH_CONNECT
                    )
                    return
                }
            }
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            findBluetoothDevice()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (REQUEST_BLUETOOTH_CONNECT == requestCode) {
            if (resultCode == RESULT_OK) {
                // Bluetooth enabled
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                // Bluetooth not enabled
            }
        } else if (REQUEST_ENABLE_BT == requestCode) {
            if (resultCode == RESULT_OK) {
                // Bluetooth enabled
                findBluetoothDevice()
            } else {
                // Bluetooth not enabled
            }
        } else if (REQUEST_LOCATION_PERMISSION == requestCode) {
            if (resultCode == RESULT_OK) {
                // Bluetooth enabled
                startDiscovery()
            } else {
                // Bluetooth not enabled
            }

        }
    }

    @SuppressLint("MissingPermission")
    fun findBluetoothDevice() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            val isBonded = device.bondState == BluetoothDevice.BOND_BONDED

            Log.d("pairedDevices", "findBluetoothDevice: $deviceName($deviceHardwareAddress)")

//            if (deviceName == "Rockerz 235V2") {
//                binding.tvPairedDeviceName.text = "$deviceName($deviceHardwareAddress)"
//                if (device.bondState != BluetoothDevice.BOND_BONDED) {
//                    Log.d("pairedDevices", "Bounding: $deviceName($deviceHardwareAddress)")
////                    device.createBond()
//                    binding.btnConnect.isVisible = true
//                    startDiscovery(deviceHardwareAddress)
//                } else {
//                    Log.d("pairedDevices", "Bounded: $deviceName($deviceHardwareAddress)")
//                    connectToDevice(device)
//                    binding.btnConnect.isVisible = false
//                }
//            }

            pairedDeviceAdapter.addDevice(DeviceInfo(deviceName, deviceHardwareAddress, isBonded))
        }
        val isOurDevicePaired =
            pairedDevices?.any { it.name == "Rockerz 235V2" }
                ?: false
        if (!isOurDevicePaired) {
            checkForLocationPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery(deviceAddress: String? = null) {
//        registerForDeviceDiscovery()
//        bluetoothAdapter?.startDiscovery()
        val intentBluetooth = Intent()
        intentBluetooth.action = Settings.ACTION_BLUETOOTH_SETTINGS
        deviceAddress?.let {
            intentBluetooth.putExtra("btcontroller", deviceAddress)
        }
        startActivity(intentBluetooth)
        Toast.makeText(
            applicationContext,
            "Pair the bluetooth device",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkForLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No BLUETOOTH_CONNECT Permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
            return
        } else {
            startDiscovery()
        }
    }

    private var connectedDeviceSocket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: DeviceInfo) {
        connectedDeviceSocket =
            bluetoothAdapter?.getRemoteDevice(device.address)
                ?.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        connectedDeviceSocket?.let { socket ->
            try {
                socket.connect()
//            val stream = socket.inputStream
//            val length = stream.available()
//            Log.d("pairedDevices", "Socket length: $length")
//            stream.close()

                val stream = socket.outputStream
//            stream.write()
                stream.close()
            } catch (exception: Exception) {
                exception.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Error: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun disconnectDevice() {
        connectedDeviceSocket?.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectDevice()
    }

    private fun registerForDeviceDiscovery() {
        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // Add the device to a list
                    device?.let {
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address // MAC address

//                        binding.tvPairedDeviceName.text = "$deviceName($deviceHardwareAddress)"
                        Log.d(
                            "pairedDevices Discovery",
                            "findBluetoothDevice: $deviceName($deviceHardwareAddress)"
                        )

                        if (deviceName == "Rockerz 235V2") {
                            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                                Log.d(
                                    "pairedDevices Discovery",
                                    "Bounding: $deviceName($deviceHardwareAddress)"
                                )
                                device.createBond()
                            } else {
                                Log.d(
                                    "pairedDevices Discovery",
                                    "Bounded: $deviceName($deviceHardwareAddress)"
                                )
                            }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    companion object {
        const val REQUEST_BLUETOOTH_CONNECT = 100
        const val REQUEST_ENABLE_BT = 101
        const val REQUEST_LOCATION_PERMISSION = 102
    }


}