package com.simple.bluetoothhub

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.snapshots.SnapshotStateList

sealed class MainUiState {
    object Loading : MainUiState()
    data class Loaded(
        val bluetoothState: BluetoothState,
        val discoveredDevices: SnapshotStateList<BluetoothDevice>,
        val pairedDevices: List<BluetoothDevice>,
        val selectedDeviceData: String?
    ) : MainUiState()
}