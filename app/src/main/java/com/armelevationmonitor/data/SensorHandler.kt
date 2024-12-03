import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.sqrt

class SensorHandler(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

        private var linearAcceleration = FloatArray(3)
    private var angularVelocity = FloatArray(3)

    var currentAngleAlgorithm1: Float = 0f
        private set
    var currentAngleAlgorithm2: Float = 0f
        private set

    private var previousFilteredAngle: Float = 0f // For EWMA filtering (Algorithm 1)
    private val alphaEWMA: Float = 0.1f // Smoothing factor for EWMA

    private var integratedGyroAngle: Float = 0f // For gyroscope integration (Algorithm 2)
    private val alphaComplementary: Float = 0.98f // Filter factor for complementary filter
    private var previousTimestamp: Long = 0L // To calculate time delta for gyroscope

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            println("Accelerometer registered.")
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            println("Gyroscope registered.")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun reset() {
        previousFilteredAngle = 0f
        integratedGyroAngle = 0f
        previousTimestamp = 0L
        linearAcceleration = FloatArray(3)
        angularVelocity = FloatArray(3)
        println("SensorHandler state has been reset.")
    }


    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                linearAcceleration = event.values.clone()
                calculateAlgorithm1()
                calculateAlgorithm2()
            }
            Sensor.TYPE_GYROSCOPE -> {
                val dt = calculateDeltaTime(event.timestamp)
                if (dt > 0) {
                    val angularVelocityZ = event.values[2] // Gyroscope rotation around Z-axis
                    integratedGyroAngle += angularVelocityZ * dt // Integrate angular velocity over time
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if necessary
    }

    private fun calculateAlgorithm1() {
        val ax = linearAcceleration[0]
        val ay = linearAcceleration[1]
        val az = linearAcceleration[2]

        // Compute raw tilt angle
        val rawAngle = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()

        // Apply EWMA filter
        currentAngleAlgorithm1 = alphaEWMA * rawAngle + (1 - alphaEWMA) * previousFilteredAngle
        previousFilteredAngle = currentAngleAlgorithm1 // Update for the next iteration
    }

    private fun calculateAlgorithm2() {
        val ax = linearAcceleration[0]
        val ay = linearAcceleration[1]
        val az = linearAcceleration[2]

        // Compute tilt angle from linear acceleration
        val accelerometerAngle = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()

        // Apply complementary filter
        currentAngleAlgorithm2 =
            alphaComplementary * integratedGyroAngle + (1 - alphaComplementary) * accelerometerAngle
    }

    private fun calculateDeltaTime(timestamp: Long): Float {
        if (previousTimestamp == 0L) {
            previousTimestamp = timestamp
            return 0f
        }
        val dt = (timestamp - previousTimestamp) / 1_000_000_000f // Convert nanoseconds to seconds
        previousTimestamp = timestamp
        return dt
    }
}
