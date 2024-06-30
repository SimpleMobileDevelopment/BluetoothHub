package com.simple.bluetoothhub

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    private var bluetoothState = BluetoothState.DEFAULT

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    private var discoveredDevices = mutableStateListOf<BluetoothDevice>()

    // TODO: look into making this more scalable
    private var selectedDevice by mutableStateOf<BluetoothDevice?>(null)
    private var selectedDeviceConnectionThread: ConnectThread? = null
    private var selectedDeviceData: String? = null

    private val _viewState: MutableStateFlow<MainUiState> =
        MutableStateFlow(
            MainUiState.Loaded(
                bluetoothState,
                discoveredDevices,
                selectedDeviceData
            )
        )
    val viewState = _viewState.asStateFlow()

    init {
        bluetoothManager = application.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            bluetoothState = BluetoothState.DISABLED
            updateViewState()
        } else {
            val isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
            if (!isBluetoothEnabled) {
                // Request to enable Bluetooth
                bluetoothState = BluetoothState.REQUEST_PERMISSION
                updateViewState()
            } else {
                bluetoothState = BluetoothState.DEFAULT
            }
        }
    }

    private fun updateViewState() {
        _viewState.value = MainUiState.Loaded(
            bluetoothState = bluetoothState,
            discoveredDevices = discoveredDevices,
            selectedDeviceData = selectedDeviceData
        )
    }

    private fun scanningFinished() {
        bluetoothState = BluetoothState.DEFAULT
    }

    @SuppressLint("MissingPermission")
    fun startDeviceDiscovery() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery()
        }
        bluetoothAdapter?.startDiscovery()
        bluetoothState = BluetoothState.DISCOVERING
        updateViewState()
    }

    /**
     * Connect to the selected device and start a server socket connection
     * and then listen to data from connected socket
     */
    @SuppressLint("MissingPermission")
    fun startBluetoothService() {
        bluetoothState = BluetoothState.CONNECTING
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter?.cancelDiscovery()
        //Listen for data from selected device
        selectedDevice?.let {
            ConnectThread(it).start()
            selectedDeviceConnectionThread = ConnectThread(it)
            selectedDeviceConnectionThread?.start()
            selectedDeviceConnectionThread?.connectionDO?.map {
                // Do something with the connection state
            }
        }
    }

    fun onDeviceSelected(device: BluetoothDevice) {
        // TODO: should we autostart device connection?
        selectedDevice = device
    }

    fun onPermissionGranted() {
        bluetoothState = BluetoothState.DEFAULT
        updateViewState()
    }

    fun onPermissionDenied() {
        bluetoothState = BluetoothState.PERMISSION_DENIED
    }

    fun onPermissionRequired() {
        bluetoothState = BluetoothState.REQUEST_PERMISSION
    }

    fun cancelDeviceConnection() {
        selectedDeviceConnectionThread?.cancelConnection()
    }
}