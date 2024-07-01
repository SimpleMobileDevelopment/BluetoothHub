package com.simple.bluetoothhub

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mainViewModel.onPermissionGranted()
        } else {
            mainViewModel.onPermissionDenied()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            mainViewModel.onBluetoothDisabled()
        } else {
            mainViewModel.onBluetoothEnabled()
        }
    }

    private val bluetoothDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (device != null && device.name != null) {
                            mainViewModel.onDeviceDiscovered(device)
                        }
                    } else {
                        mainViewModel.onPermissionRequired()
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    mainViewModel.onDeviceDiscoveryFinished()
                }

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    mainViewModel.checkBluetoothAdapterState()
                }
            }
        }
    }

    private fun enableBluetooth() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for broadcasts when a device is discovered
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(bluetoothDeviceReceiver, filter)

        setContent {
            val viewState by mainViewModel.viewState.collectAsStateWithLifecycle()

            MainScreen(
                viewState = viewState,
                onStartScan = {
                    requestPermissions(
                        multiplePermissionLauncher,
                        requiredPermissionsInitialClient,
                        this
                    ) { mainViewModel.startDeviceDiscovery() }
                },
                onStopScan = { mainViewModel.cancelDeviceConnection() },
                onDeviceSelected = { mainViewModel.onDeviceSelected(it) },
                onPermissionRequired = { mainViewModel.onPermissionRequired() },
                onRequestPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                },
                onBluetoothDisabled = { enableBluetooth() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.cancelDeviceDiscovery()
        mainViewModel.cancelDeviceConnection()
        this.unregisterReceiver(bluetoothDeviceReceiver)
    }

    private fun requestPermissions(
        multiplePermissionLauncher: ActivityResultLauncher<Array<String>>,
        requiredPermissions: Array<String>,
        context: Context,
        actionIfAlreadyGranted: () -> Unit
    ) {
        if (!hasPermissions(requiredPermissions, context)) {
            multiplePermissionLauncher.launch(requiredPermissions)
        } else {
            actionIfAlreadyGranted()
        }
    }

    private fun hasPermissions(permissions: Array<String>?, context: Context): Boolean {
        return permissions?.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }

    private val multiplePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.containsValue(false)) {
                mainViewModel.onPermissionDenied()
            } else {
                mainViewModel.startDeviceDiscovery()
            }
        }

}