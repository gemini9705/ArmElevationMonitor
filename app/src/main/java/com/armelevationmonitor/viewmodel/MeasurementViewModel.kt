import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

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
            sensorHandler.start()
            isConnected.value = true
        }
    }

    fun startMeasurement() {
        if (!isConnected.value) return // Ensure the sensor is connected

        // Clear previous data only when starting a new session
        measurementDataAlgorithm1.clear()
        measurementDataAlgorithm2.clear()

        // Start sensors and initiate data collection
        sensorHandler.start()

        // Start recording data in a coroutine
        measurementJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val angle1 = sensorHandler.currentAngleAlgorithm1
                val angle2 = sensorHandler.currentAngleAlgorithm2
                withContext(Dispatchers.Main) {
                    angleAlgorithm1.value = angle1
                    angleAlgorithm2.value = angle2
                    measurementDataAlgorithm1.add(Pair(currentTime, angle1))
                    measurementDataAlgorithm2.add(Pair(currentTime, angle2))
                }
                delay(100) // Record data every 100ms
            }
        }
    }


    fun stopMeasurement() {
        // Stop the measurement coroutine if it's active
        measurementJob?.cancel()
        measurementJob = null

        // Stop sensor updates
        sensorHandler.stop()

        // Reset the angle states for the UI
        angleAlgorithm1.value = 0f
        angleAlgorithm2.value = 0f

        // Recorded data is not cleared and is available for export
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
            file.absolutePath // Return the file path for reference
        } catch (e: Exception) {
            e.printStackTrace()
            "Error exporting data: ${e.message}" // Return an error message
        }
    }
}
