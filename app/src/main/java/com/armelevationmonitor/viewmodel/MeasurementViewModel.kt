import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.Timer
import java.util.TimerTask

class MeasurementViewModel(context: Context) : ViewModel() {
    private val sensorHandler = SensorHandler(context)
    val angleAlgorithm1 = mutableStateOf(0f) // For Algorithm 1
    val angleAlgorithm2 = mutableStateOf(0f) // For Algorithm 2
    val measurementDataAlgorithm1 = mutableListOf<Pair<Long, Float>>()
    val measurementDataAlgorithm2 = mutableListOf<Pair<Long, Float>>()
    val isConnected = mutableStateOf(false)
    private var timer: Timer? = null

    fun connectSensor() {
        if (!isConnected.value) {
            sensorHandler.start()
            isConnected.value = true
        }
    }

    fun startMeasurement() {
        if (!isConnected.value) return // Ensure the sensor is connected
        sensorHandler.start()
        measurementDataAlgorithm1.clear()
        measurementDataAlgorithm2.clear()
        timer?.cancel()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    val currentTime = System.currentTimeMillis()
                    val angle1 = sensorHandler.currentAngleAlgorithm1
                    val angle2 = sensorHandler.currentAngleAlgorithm2

                    // Update mutable state values for the UI
                    angleAlgorithm1.value = angle1
                    angleAlgorithm2.value = angle2

                    // Record measurements for export
                    measurementDataAlgorithm1.add(Pair(currentTime, angle1))
                    measurementDataAlgorithm2.add(Pair(currentTime, angle2))
                }
            }, 0, 100) // Collect data every 100ms
        }
    }

    fun stopMeasurement() {
        timer?.cancel()
        timer = null
        sensorHandler.stop()
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
