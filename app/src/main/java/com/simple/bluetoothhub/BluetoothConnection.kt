package com.simple.bluetoothhub

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

class BluetoothConnection(
    private val socket: BluetoothSocket,
    private val onDataReceived: (connectionDO: ConnectionDO) -> Unit,
) : Thread() {
    private val inputStream = socket.inputStream

    // TODO: determine how best to read data
    private val buffer = ByteArray(1)
    override fun run() {
        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                inputStream.read(buffer)
            } catch (e: IOException) {
                Log.i("BluetoothConnection", "Input stream has been disconnected", e)
                break
            }
            val text = String(buffer)
            onDataReceived(ConnectionDO(DeviceConnectionState.LOADED, text))
        }
    }
}

/**
 * A Thread to create a connection as a Client to an existing Server (the Bluetooth device).
 *
 * @param device The BluetoothDevice to connect to.
 */
@SuppressLint("MissingPermission")
class ConnectThread(
    device: BluetoothDevice
) : Thread() {

    private val _connectionDO = MutableStateFlow(ConnectionDO(DeviceConnectionState.STARTED, null))
    val connectionDO = _connectionDO.asStateFlow()

    private val bluetoothSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(applicationUUID)
    }

    override fun run() {
        bluetoothSocket?.let { socket ->
            //Connect to the remote device through the socket.
            // This call blocks until it succeeds or throws an exception
            try {
                socket.connect()
            } catch (e: Exception) {
                updateConnectionState(
                    ConnectionDO(
                        DeviceConnectionState.ERROR,
                        "Error on connectivity: $e"
                    )
                )
            }
            BluetoothConnection(socket) { result -> updateConnectionState(result) }.start()
        }
    }

    private fun updateConnectionState(DO: ConnectionDO) {
        _connectionDO.value = DO
    }

    fun cancelConnection() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("BluetoothConnection", "Unable to close cancel connection", e)
        }
    }
}