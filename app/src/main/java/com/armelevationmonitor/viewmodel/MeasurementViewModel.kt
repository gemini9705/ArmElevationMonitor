import android.content.Context
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

        // Stop any previous measurement job if it exists
        stopMeasurement()

        // Clear previous data
        measurementDataAlgorithm1.clear()
        measurementDataAlgorithm2.clear()

        // Register sensors and start fetching data
        sensorHandler.startMeasurement()

        // Start the coroutine for periodic updates
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

                delay(100) // Update every 100ms
            }
        }
    }

    fun stopMeasurement() {
        measurementJob?.cancel()
        measurementJob = null
        sensorHandler.stopMeasurement()
        println("Measurement stopped.")
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
}
