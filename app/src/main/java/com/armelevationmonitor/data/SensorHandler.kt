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

    private val gravity = FloatArray(3) // Gravity vector for fallback logic
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
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // Fallback
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            println("Accelerometer registered.")
        } ?: println("No accelerometer available.")
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
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

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                linearAcceleration = event.values.clone()
                calculateAlgorithm1()
                calculateAlgorithm2()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                computeLinearAcceleration(event)
                calculateAlgorithm1()
                calculateAlgorithm2()
            }
            Sensor.TYPE_GYROSCOPE -> {
                val dt = calculateDeltaTime(event.timestamp)
                if (dt > 0) {
                    val angularVelocityZ = event.values[2]
                    integratedGyroAngle += angularVelocityZ * dt
                }
            }
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
        currentAngleAlgorithm1 = alphaEWMA * rawAngle + (1 - alphaEWMA) * previousFilteredAngle
        previousFilteredAngle = currentAngleAlgorithm1
    }

    private fun calculateAlgorithm2() {
        val ax = linearAcceleration[0]
        val ay = linearAcceleration[1]
        val az = linearAcceleration[2]

        val accelerometerAngle = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()
        currentAngleAlgorithm2 =
            alphaComplementary * integratedGyroAngle + (1 - alphaComplementary) * accelerometerAngle
    }

    private fun calculateDeltaTime(timestamp: Long): Float {
        if (previousTimestamp == 0L) {
            previousTimestamp = timestamp
            return 0f
        }
        val dt = (timestamp - previousTimestamp) / 1_000_000_000f
        previousTimestamp = timestamp
        return dt
    }
}

