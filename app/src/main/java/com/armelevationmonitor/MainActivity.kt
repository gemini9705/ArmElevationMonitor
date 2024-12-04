package com.armelevationmonitor

import MainScreen
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logAvailableSensors(this) // Log available sensors for debugging
        setContent {
            requestPermissions(this) // Request permissions here
            MaterialTheme {
                Surface {
                    MainScreen(context = this)
                }
            }
        }
    }

}

fun requestPermissions(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val permissions = mutableListOf<String>()
        if (activity.checkSelfPermission(android.Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.BODY_SENSORS)
        }
        if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (activity.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissions.isNotEmpty()) {
            activity.requestPermissions(permissions.toTypedArray(), 1)
        }
    }
}

fun logAvailableSensors(context: Context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
    println("Available Sensors:")
    sensors.forEach { sensor ->
        println("Sensor: ${sensor.name}, Type: ${sensor.type}")
    }
}
