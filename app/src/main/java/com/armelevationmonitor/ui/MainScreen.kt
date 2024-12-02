import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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

            // Export Data for Algorithm 1
            Button(
                onClick = {
                    val result = viewModel.exportData(context, algorithm = 1) // Export Algorithm 1 data
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected && hasDataAlgorithm1 // Enable only if connected and data exists for Algorithm 1
            ) {
                Text("Export Algorithm 1 Data")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export Data for Algorithm 2
            Button(
                onClick = {
                    val result = viewModel.exportData(context, algorithm = 2) // Export Algorithm 2 data
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected && hasDataAlgorithm2 // Enable only if connected and data exists for Algorithm 2
            ) {
                Text("Export Algorithm 2 Data")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Angles
            Text(text = "Angle (Algorithm 1): ${"%.2f".format(angleAlgorithm1)}°") // Show Algorithm 1 angle
            Text(text = "Angle (Algorithm 2): ${"%.2f".format(angleAlgorithm2)}°") // Show Algorithm 2 angle
        }
    }
}

