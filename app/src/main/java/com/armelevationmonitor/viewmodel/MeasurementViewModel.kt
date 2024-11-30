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

    fun connectSensor() {
        sensorHandler.start()
    }

    fun startMeasurement() {
        measurementData.clear()
        // Simulate real-time data collection
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val currentAngle = sensorHandler.currentAngle
                angle.value = currentAngle
                measurementData.add(Pair(currentTime, currentAngle))
            }
        }, 0, 100) // Update every 100ms
    }

    fun stopMeasurement() {
        sensorHandler.stop()
    }

    fun exportData(context: Context) {
        val fileName = "angle_data_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        file.bufferedWriter().use { writer ->
            writer.write("Timestamp,Angle\n")
            measurementData.forEach { (timestamp, angle) ->
                writer.write("$timestamp,$angle\n")
            }
        }
    }
}
