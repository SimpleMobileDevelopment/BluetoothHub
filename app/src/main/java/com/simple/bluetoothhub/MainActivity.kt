package com.simple.bluetoothhub

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private var isScanning by mutableStateOf(false)

    private val mainViewModel: MainViewModel by viewModels()

    // Launcher for requesting location permission (needed for Bluetooth scanning)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with Bluetooth operations
            mainViewModel.onPermissionGranted()
        } else {
            // Handle case where permission is denied
            mainViewModel.onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewState by mainViewModel.viewState.collectAsStateWithLifecycle()

            MainScreen(
                viewState = viewState,
                isScanning = isScanning,
                onStartScan = {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onStopScan = { mainViewModel.cancelDeviceConnection() },
                onDeviceSelected = { mainViewModel.onDeviceSelected(it) }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        viewState: MainUiState,
        isScanning: Boolean,
        onStartScan: () -> Unit,
        onStopScan: () -> Unit,
        onDeviceSelected: (BluetoothDevice) -> Unit
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
                if (isScanning) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = onStartScan) {
                        Text("Start Scan")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (viewState is MainUiState.Loaded) {
                    DiscoveredDevices(viewState = viewState) {
                        mainViewModel.onDeviceSelected(it)
                    }
                }

                if (isScanning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onStopScan) {
                        Text("Stop Scan")
                    }
                }
            }
        }
    }

    @Composable
    fun DiscoveredDevices(viewState: MainUiState.Loaded, onDeviceSelected: (BluetoothDevice) -> Unit) {
        val context = LocalContext.current

        viewState.discoveredDevices.forEach { device ->
            Button(onClick = { onDeviceSelected(device) }) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    mainViewModel.onPermissionRequired()
                } else {
                    Text(device.name ?: "Unknown Device")
                }
            }
        }
    }
}