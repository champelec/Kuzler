import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    var subject by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Введите данные", fontSize = 24.sp, color = Color.Blue)

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Название предмета") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Стоимость") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { sendDataToServer(subject, price) }) {
            Text("Отправить данные")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { readNFC() }) {
            Text("Считать NFC метку")
        }
    }
}

fun sendDataToServer(subject: String, price: String) {
    // Здесь вы можете реализовать логику отправки данных на сервер через Retrofit
    CoroutineScope(Dispatchers.IO).launch {
        // Например, здесь должны быть вызовы к API.
    }
}

fun readNFC() {
    // Реализация считывания NFC метки.
}
