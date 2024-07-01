package com.simple.bluetoothhub

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewState: MainUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onPermissionRequired: () -> Unit,
    onRequestPermission: () -> Unit,
    onBluetoothDisabled: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bluetooth App") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (viewState) {
                is MainUiState.Loaded -> {
                    LoadedScreen(
                        viewState = viewState,
                        onStartScan = onStartScan,
                        onStopScan = onStopScan,
                        onDeviceSelected = onDeviceSelected,
                        onRequestPermission = onRequestPermission,
                        onPermissionRequired = onPermissionRequired,
                        onBluetoothDisabled = onBluetoothDisabled
                    )
                }

                else -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun LoadedScreen(
    viewState: MainUiState.Loaded,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onPermissionRequired: () -> Unit,
    onRequestPermission: () -> Unit,
    onBluetoothDisabled: () -> Unit
) {
    when (viewState.bluetoothState) {
        BluetoothState.DEFAULT -> {
            Button(onClick = onStartScan) {
                Text("Start Scan")
            }
        }

        BluetoothState.REQUEST_PERMISSION -> {
            Text("Request Permission")
            Button(onClick = onRequestPermission) {
                Text("Request Permission")
            }
        }

        BluetoothState.PERMISSION_DENIED -> {
            Text("Permission Denied")
            Button(onClick = onRequestPermission) {
                Text("Request Permission")
            }
        }

        BluetoothState.DISCOVERING -> {
            CircularProgressIndicator()
            Text("Discovering")
            Button(onClick = onStopScan) {
                Text("Stop Scan")
            }
        }

        BluetoothState.CONNECTING -> {
            Text("Connecting")
        }

        BluetoothState.ERROR -> {
            Text("Error")
        }

        BluetoothState.DISABLED -> {
            Text("Bluetooth Disabled")
            onBluetoothDisabled.invoke()
        }

        BluetoothState.UNAVAILABLE -> {
            Text("Unavailable")
        }

        else -> {

        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    DiscoveredDevices(
        viewState = viewState,
        onDeviceSelected = onDeviceSelected,
        onPermissionRequired = onPermissionRequired
    )
}

@Composable
fun DiscoveredDevices(
    viewState: MainUiState.Loaded,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onPermissionRequired: () -> Unit
) {
    val context = LocalContext.current

    viewState.discoveredDevices.forEach { device ->
        Button(onClick = { onDeviceSelected(device) }) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionRequired.invoke()
            } else {
                Text(device.name ?: "Unknown Device")
            }
        }
    }
}