package com.example.myapplicationforkuzlerver1

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private var nfcTagId by mutableStateOf("")
    private var nfcStatus by mutableStateOf("Нажмите 'Считать NFC'")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Обработка NFC метки при запуске
        if (intent?.action in listOf(
                NfcAdapter.ACTION_TAG_DISCOVERED,
                NfcAdapter.ACTION_NDEF_DISCOVERED,
                NfcAdapter.ACTION_TECH_DISCOVERED
            )) {
            handleNfcIntent(intent)
        }

        setContent {
            val context = LocalContext.current
            var productName by remember { mutableStateOf("") }
            var productPrice by remember { mutableStateOf("") }

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Добавление товара", fontSize = 24.sp, color = Color.Blue)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Название товара") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it },
                        label = { Text("Цена") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (nfcTagId.isEmpty()) {
                                Toast.makeText(context, "Сначала считайте NFC метку!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (productName.isBlank() || productPrice.isBlank()) {
                                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            sendDataToServer(productName, productPrice, context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сохранить товар")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { checkNfcAvailability(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Считать NFC метку")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = nfcStatus,
                        color = when {
                            nfcStatus.contains("Готов") -> Color.Blue
                            nfcStatus.contains("Метка") -> Color.Green
                            else -> Color.Red
                        }
                    )

                    if (nfcTagId.isNotEmpty()) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ID метки:", fontSize = 16.sp)
                            Text(
                                text = nfcTagId,
                                color = Color.Green,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            null,
            arrayOf(
                arrayOf("android.nfc.tech.NfcA"),
                arrayOf("android.nfc.tech.NfcB"),
                arrayOf("android.nfc.tech.NfcF"),
                arrayOf("android.nfc.tech.NfcV"),
                arrayOf("android.nfc.tech.IsoDep"),
                arrayOf("android.nfc.tech.Ndef")
            )
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action in listOf(
                NfcAdapter.ACTION_TAG_DISCOVERED,
                NfcAdapter.ACTION_NDEF_DISCOVERED,
                NfcAdapter.ACTION_TECH_DISCOVERED
            )) {
            handleNfcIntent(intent)
        }
    }

    private fun checkNfcAvailability(context: android.content.Context) {
        when {
            nfcAdapter == null -> {
                nfcStatus = "NFC не поддерживается"
                Toast.makeText(context, "Ваше устройство не поддерживает NFC", Toast.LENGTH_SHORT).show()
            }
            !nfcAdapter!!.isEnabled -> {
                nfcStatus = "NFC выключен"
                Toast.makeText(context, "Включите NFC в настройках", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
            else -> {
                nfcStatus = "Готов к сканированию..."
                Toast.makeText(context, "Поднесите NFC метку", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleNfcIntent(intent: Intent) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tag?.let {
            nfcTagId = it.id.toHexString()
            nfcStatus = "Метка считана! ID: ${nfcTagId}"
        }
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

    private fun sendDataToServer(name: String, price: String, context: android.content.Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.createProduct(
                    ProductData(
                        nfc_id = nfcTagId,
                        name = name,
                        price = price
                    )
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Товар сохранен! ID: ${response.body()?.id}",
                            Toast.LENGTH_LONG
                        ).show()
                        nfcStatus = "Данные отправлены!"
                    } else {
                        Toast.makeText(
                            context,
                            "Ошибка: ${response.code()} ${response.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка HTTP: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}