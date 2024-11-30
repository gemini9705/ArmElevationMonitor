import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorHandler(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    // Variables to hold processed data
    private var linearAcceleration = FloatArray(3)
    private var angularVelocity = FloatArray(3)

    var currentAngle: Float = 0f
        private set

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    fun start() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                linearAcceleration = event.values.clone()
                calculateAngle()
            }
            Sensor.TYPE_GYROSCOPE -> {
                angularVelocity = event.values.clone()
                // Optionally, combine gyroscope data for more precise angles
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle changes in sensor accuracy if necessary
    }

    private fun calculateAngle() {
        // Basic tilt angle calculation using linear acceleration
        val ax = linearAcceleration[0]
        val ay = linearAcceleration[1]
        val az = linearAcceleration[2]

        // Compute the tilt angle relative to gravity
        currentAngle = Math.toDegrees(Math.atan2(ay.toDouble(), Math.sqrt((ax * ax + az * az).toDouble()))).toFloat()
    }
}
