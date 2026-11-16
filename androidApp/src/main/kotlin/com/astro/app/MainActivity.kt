package com.astro.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.astro.app.data.sensors.VitalsMonitor
import com.astro.app.presentation.navigation.AppNavHost
import com.astro.app.presentation.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val vitals: VitalsMonitor by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val sensor = heartRatePermission()
        val permissions = requiredPermissions(sensor)
        if (isGranted(sensor)) vitals.start(sensorPermitted = true)
        setContent {
            AppTheme {
                AppNavHost(
                    permissions = permissions,
                    sensorPermission = sensor,
                    hasPermission = { permissions.all { isGranted(it) } },
                    // Denied (or permanently denied): HR falls back to simulation.
                    onPermissionResult = { granted -> vitals.start(sensorPermitted = granted) },
                )
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) vitals.stop()
        super.onDestroy()
    }

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    // Every runtime permission the app needs: the heart-rate sensor, plus notifications where they are requestable.
    private fun requiredPermissions(sensor: String): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(sensor, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(sensor)
        }

    // Granular health permission on newer platforms, body sensors before.
    private fun heartRatePermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) READ_HEART_RATE else Manifest.permission.BODY_SENSORS

    private companion object {
        const val READ_HEART_RATE = "android.permission.health.READ_HEART_RATE"
    }
}
