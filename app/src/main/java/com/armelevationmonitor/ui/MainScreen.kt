import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxWidth

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context) {
    // Create ViewModel using the factory
    val factory = MeasurementViewModelFactory(context)
    val viewModel: MeasurementViewModel = viewModel(factory = factory)

    // Directly observe mutableStateOf values
    val isConnected = viewModel.isConnected.value
    val angleAlgorithm1 = viewModel.angleAlgorithm1.value
    val angleAlgorithm2 = viewModel.angleAlgorithm2.value
    val hasDataAlgorithm1 = viewModel.measurementDataAlgorithm1.isNotEmpty()
    val hasDataAlgorithm2 = viewModel.measurementDataAlgorithm2.isNotEmpty()

    // Extract graph data
    val graphDataAlg1 = viewModel.measurementDataAlgorithm1.map { it.second }
    val graphDataAlg2 = viewModel.measurementDataAlgorithm2.map { it.second }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Arm Elevation Monitor") })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Connect Button
            Button(
                onClick = { viewModel.connectSensor() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConnected
            ) {
                Text(if (isConnected) "Connected" else "Connect")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Measurement Button
            Button(
                onClick = { viewModel.startMeasurement() },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text("Start Measurement")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stop Measurement Button
            Button(
                onClick = { viewModel.stopMeasurement() },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text("Stop Measurement")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // UI Buttons for Export
            Button(
                onClick = {
                    val result = viewModel.exportData(context, algorithm = 1) // Export Algorithm 1 data
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasDataAlgorithm1 // Enable if Algorithm 1 has data
            ) {
                Text("Export Algorithm 1 Data")
            }

            Button(
                onClick = {
                    val result = viewModel.exportData(context, algorithm = 2) // Export Algorithm 2 data
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasDataAlgorithm2 // Enable if Algorithm 2 has data
            ) {
                Text("Export Algorithm 2 Data")
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Display Angles
            Text(text = "Angle (Algorithm 1): ${"%.2f".format(angleAlgorithm1)}°")
            Text(text = "Angle (Algorithm 2): ${"%.2f".format(angleAlgorithm2)}°")

            Spacer(modifier = Modifier.height(16.dp))

            // Graph Display for Algorithm 1
            Text(text = "Live Graph (Algorithm 1)")
            Spacer(modifier = Modifier.height(8.dp))
            LiveGraph(graphDataAlg1, lineColor = Color.Blue)

            Spacer(modifier = Modifier.height(16.dp))

            // Graph Display for Algorithm 2
            Text(text = "Live Graph (Algorithm 2)")
            Spacer(modifier = Modifier.height(8.dp))
            LiveGraph(graphDataAlg2, lineColor = Color.Red)
        }
    }
}

@Composable
fun LiveGraph(data: List<Float>, lineColor: Color = Color.Blue) {
    if (data.isEmpty()) {
        Text("No data to display")
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val path = Path().apply {
            moveTo(0f, size.height / 2) // Start at the center of the canvas
            data.forEachIndexed { index, value ->
                val x = size.width * index / data.size // Scale X
                val y = size.height / 2 - value * 5 // Scale Y and invert direction
                lineTo(x, y)
            }
        }
        drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
    }
}