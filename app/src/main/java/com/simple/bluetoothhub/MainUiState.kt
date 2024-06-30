package com.simple.bluetoothhub

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.snapshots.SnapshotStateList

sealed class MainUiState {
    data class Loaded(
        val bluetoothState: BluetoothState,
        val discoveredDevices: SnapshotStateList<BluetoothDevice>,
        val selectedDeviceData: String?
    ) : MainUiState()
}