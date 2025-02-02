import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.io.File

class MeasurementViewModel(context: Context) : ViewModel() {
    private val sensorHandler = SensorHandler(context)
    val angleAlgorithm1 = mutableStateOf(0f) // For Algorithm 1
    val angleAlgorithm2 = mutableStateOf(0f) // For Algorithm 2
    val measurementDataAlgorithm1 = mutableListOf<Pair<Long, Float>>()
    val measurementDataAlgorithm2 = mutableListOf<Pair<Long, Float>>()
    val isConnected = mutableStateOf(false)
    private var measurementJob: Job? = null

    fun connectSensor() {
        val context = sensorHandler.context // Pass the context if needed
        if (!isSensorAvailable(context, Sensor.TYPE_LINEAR_ACCELERATION)) {
            println("Linear Acceleration sensor not available on this device.")
            return
        }
        if (!isSensorAvailable(context, Sensor.TYPE_GYROSCOPE)) {
            println("Gyroscope sensor not available on this device.")
            return
        }

        if (!isConnected.value) {
            isConnected.value = true
            println("Sensors connected.")
        } else {
            println("Sensors already connected.")
        }
    }


    fun startMeasurement() {
        if (!isConnected.value) {
            println("Cannot start measurement: Sensor not connected.")
            return
        }

        measurementJob?.cancel() // Stop any previous job
        sensorHandler.reset() // Reset the sensor state

        measurementDataAlgorithm1.clear()
        measurementDataAlgorithm2.clear()

        measurementJob = CoroutineScope(Dispatchers.Default).launch {
            println("Measurement coroutine started.")
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val angle1 = sensorHandler.currentAngleAlgorithm1
                val angle2 = sensorHandler.currentAngleAlgorithm2

                withContext(Dispatchers.Main) {
                    angleAlgorithm1.value = angle1
                    angleAlgorithm2.value = angle2
                    measurementDataAlgorithm1.add(Pair(currentTime, angle1))
                    measurementDataAlgorithm2.add(Pair(currentTime, angle2))
                    println("Measurement updated. Algorithm1: $angle1, Algorithm2: $angle2")
                }
                delay(200) // Adjusted for performance
            }
        }
    }

    fun stopMeasurement() {
        if (measurementJob != null) {
            measurementJob?.cancel()
            measurementJob = null
            println("Measurement coroutine stopped.")
        } else {
            println("No active measurement job to stop.")
        }

        sensorHandler.stop() // Stop sensor updates
        println("SensorHandler stopped.")

        // Keep measurement data intact; no clearing here
        println("Measurement data preserved.")

        // Reset angles on UI if necessary
        angleAlgorithm1.value = 0f
        angleAlgorithm2.value = 0f
    }

    fun exportData(context: Context, algorithm: Int): String {
        return try {
            val fileName = "angle_data_${System.currentTimeMillis()}_algorithm${algorithm}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.bufferedWriter().use { writer ->
                writer.write("Timestamp,Angle\n")
                val dataToExport = if (algorithm == 1) {
                    measurementDataAlgorithm1
                } else {
                    measurementDataAlgorithm2
                }
                dataToExport.forEach { (timestamp, angle) ->
                    writer.write("$timestamp,$angle\n")
                }
            }
            println("Data exported successfully to $fileName")
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            "Error exporting data: ${e.message}"
        }
    }

    fun isSensorAvailable(context: Context, sensorType: Int): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getDefaultSensor(sensorType) != null
    }

}

