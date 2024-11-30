import android.annotation.SuppressLint
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
fun MainScreen(viewModel: MeasurementViewModel = viewModel()) {
    val context = LocalContext.current
    // Directly observe mutableStateOf values
    val isConnected = viewModel.isConnected.value
    val angle = viewModel.angle.value

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

            // Export Data Button
            Button(
                onClick = { viewModel.exportData(context) }, // Pass `context` here directly
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text("Export Data")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Angle Display
            Text(text = "Current Angle: $angle°")
        }
    }
}

