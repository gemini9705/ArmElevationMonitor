import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.sqrt

class SensorHandler(val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // Fallback
    private var gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val gravity = FloatArray(3) // Gravity vector for fallback logic
    private var linearAcceleration = FloatArray(3)
    private var angularVelocity = FloatArray(3)

    var currentAngleAlgorithm1: Float = 0f
        private set
    var currentAngleAlgorithm2: Float = 0f
        private set

    private var previousFilteredAngle: Float = 0f // For EWMA filtering (Algorithm 1)
    private val alphaEWMA: Float = 0.1f // Reduced smoothing factor for more stability

    private var integratedGyroAngle: Float = 0f // For gyroscope integration (Algorithm 2)
    private val alphaComplementary: Float = 0.95f // Reduced reliance on gyroscope
    private var previousTimestamp: Long = 0L // To calculate time delta for gyroscope

    private var angleBiasAlgorithm1: Float = 0f // For calibration
    private var angleBiasAlgorithm2: Float = 0f // For calibration

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            println("Accelerometer registered.")
        } ?: println("No accelerometer available.")

        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            println("Gyroscope registered.")
        } ?: println("No gyroscope available.")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        println("Sensors unregistered.")
    }

    fun reset() {
        stop()
        previousFilteredAngle = 0f
        integratedGyroAngle = 0f
        previousTimestamp = 0L
        linearAcceleration.fill(0f)
        angularVelocity.fill(0f)
        println("SensorHandler state has been reset.")
        start()
    }

    fun calibrate() {
        // Perform calibration by capturing current angles as bias
        angleBiasAlgorithm1 = currentAngleAlgorithm1
        angleBiasAlgorithm2 = currentAngleAlgorithm2
        println("Calibration completed. Biases set - Algorithm1: $angleBiasAlgorithm1, Algorithm2: $angleBiasAlgorithm2")
    }

    override fun onSensorChanged(event: SensorEvent) {
        try {
            when (event.sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    linearAcceleration = event.values.clone()
                    calculateAlgorithm1()
                    calculateAlgorithm2()
                    println("Linear Acceleration updated: x=${linearAcceleration[0]}, y=${linearAcceleration[1]}, z=${linearAcceleration[2]}")
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    computeLinearAcceleration(event)
                    calculateAlgorithm1()
                    calculateAlgorithm2()
                    println("Accelerometer updated: x=${event.values[0]}, y=${event.values[1]}, z=${event.values[2]}")
                }
                Sensor.TYPE_GYROSCOPE -> {
                    val dt = calculateDeltaTime(event.timestamp)
                    if (dt > 0) {
                        val angularVelocityZ = event.values[2]
                        integratedGyroAngle += angularVelocityZ * dt
                        println("Gyroscope updated: angularVelocityZ=$angularVelocityZ, integratedAngle=$integratedGyroAngle")
                    } else {
                        println("Gyroscope update skipped due to invalid delta time.")
                    }
                }
                else -> {
                    println("Unhandled sensor type: ${event.sensor.type}")
                }
            }
        } catch (e: Exception) {
            println("Error processing sensor data: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println("Sensor: ${sensor?.name}, Accuracy: $accuracy")
    }

    private fun computeLinearAcceleration(event: SensorEvent) {
        val alpha = 0.8f // Low-pass filter constant
        for (i in 0..2) {
            gravity[i] = alpha * gravity[i] + (1 - alpha) * event.values[i]
            linearAcceleration[i] = event.values[i] - gravity[i]
        }
    }

    private fun calculateAlgorithm1() {
        val ax = linearAcceleration[0]
        val ay = linearAcceleration[1]
        val az = linearAcceleration[2]

        val rawAngle = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()
        currentAngleAlgorithm1 = alphaEWMA * rawAngle + (1 - alphaEWMA) * previousFilteredAngle - angleBiasAlgorithm1
        previousFilteredAngle = currentAngleAlgorithm1
    }

    private fun calculateAlgorithm2() {
        val ax = linearAcceleration[0]
        val ay = linearAcceleration[1]
        val az = linearAcceleration[2]

        val accelerometerAngle = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()
        currentAngleAlgorithm2 =
            alphaComplementary * integratedGyroAngle + (1 - alphaComplementary) * accelerometerAngle - angleBiasAlgorithm2
    }

    private fun calculateDeltaTime(timestamp: Long): Float {
        if (previousTimestamp == 0L) {
            previousTimestamp = timestamp
            return 0f
        }
        val dt = (timestamp - previousTimestamp) / 1_000_000_000f
        previousTimestamp = timestamp
        return maxOf(dt, 1e-6f) // Prevent zero or negative values
    }

    fun isSensorAvailable(sensorType: Int): Boolean {
        return sensorManager.getDefaultSensor(sensorType) != null
    }
}
