import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.Timer
import java.util.TimerTask

class MeasurementViewModel(context: Context) : ViewModel() {
    private val sensorHandler = SensorHandler(context)
    val angle = mutableStateOf(0f)
    val measurementData = mutableListOf<Pair<Long, Float>>()
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
        measurementData.clear()
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val currentTime = System.currentTimeMillis()
                    val currentAngle = sensorHandler.currentAngle
                    angle.value = currentAngle
                    measurementData.add(Pair(currentTime, currentAngle))
                }
            }, 0, 100) // Collect data every 100ms
        }
    }

    fun stopMeasurement() {
        timer?.cancel()
        timer = null
        sensorHandler.stop()
    }

    fun exportData(context: Context): String {
        return try {
            val fileName = "angle_data_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.bufferedWriter().use { writer ->
                writer.write("Timestamp,Angle\n")
                measurementData.forEach { (timestamp, angle) ->
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
