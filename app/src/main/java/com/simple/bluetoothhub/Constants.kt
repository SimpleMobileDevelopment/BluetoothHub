package com.simple.bluetoothhub

import android.os.Build
import java.util.UUID

val applicationUUID: UUID = UUID.fromString("8f49458d-2120-490d-839a-94245945487b")

val requiredPermissionsInitialClient: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            "Manifest.permission.BLUETOOTH_CONNECT",
            "Manifest.permission.BLUETOOTH_SCAN",
        )
    } else {
        arrayOf(
            "Manifest.permission.BLUETOOTH",
            "Manifest.permission.ACCESS_FINE_LOCATION",
            "Manifest.permission.ACCESS_COARSE_LOCATION"
        )
    }