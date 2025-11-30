package com.example.waterhelp

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterhelp.data.AppDatabase
import com.example.waterhelp.data.PreferencesManager
import com.example.waterhelp.data.WaterRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

// --- VIEWMODEL: Lógica de negocio ---
class MainViewModel(private val db: AppDatabase, private val prefs: PreferencesManager) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    val dailyLimit = prefs.limitFlow.stateIn(viewModelScope, SharingStarted.Lazily, 100.0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayConsumption = _selectedDate.flatMapLatest { date ->
        db.dao().getLitersByDate(date.toEpochDay()).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val history = db.dao().getHistory(LocalDate.now().minusDays(6).toEpochDay())
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setDate(date: LocalDate) { _selectedDate.value = date }

    fun addWater(liters: Double) {
        viewModelScope.launch {
            db.dao().insert(WaterRecord(date = _selectedDate.value.toEpochDay(), liters = liters))
        }
    }

    fun updateLimit(limit: Double) {
        viewModelScope.launch { prefs.saveLimit(limit) }
    }
}

class ViewModelFactory(val db: AppDatabase, val prefs: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(db, prefs) as T
}

// --- UI PRINCIPAL ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.get(applicationContext)
        val prefs = PreferencesManager(applicationContext)
        val factory = ViewModelFactory(db, prefs)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WaterScreen(viewModel(factory = factory))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterScreen(viewModel: MainViewModel) {
    val date by viewModel.selectedDate.collectAsState()
    val consumption by viewModel.todayConsumption.collectAsState()
    val limit by viewModel.dailyLimit.collectAsState()
    val history by viewModel.history.collectAsState()

    var showTips by remember { mutableStateOf(false) }
    var litersInput by remember { mutableStateOf("") }
    var limitInput by remember { mutableStateOf("") }

    val diff = limit - consumption
    val percentage = if (limit > 0) (abs(diff) / limit) * 100 else 0.0
    val isOverLimit = consumption > limit

    val message = if (isOverLimit) {
        "⚠️ Cuidado: Has consumido un ${String.format("%.0f", percentage)}% MÁS de tu límite."
    } else {
        "✅ Bien hecho: Te queda un ${String.format("%.0f", percentage)}% de tu límite."
    }
    val messageColor = if (isOverLimit) Color(0xFFB00020) else Color(0xFF2E7D32)

    val context = LocalContext.current
    val dateDialog = DatePickerDialog(context, { _, y, m, d ->
        viewModel.setDate(LocalDate.of(y, m + 1, d))
    }, date.year, date.monthValue - 1, date.dayOfMonth)

    if (showTips) {
        AlertDialog(
            onDismissRequest = { showTips = false },
            title = { Text("💧 Consejos para Ahorrar") },
            text = {
                Column {
                    Text("• Cierra el grifo al lavarte los dientes.")
                    Spacer(Modifier.height(4.dp))
                    Text("• Toma duchas de 5 minutos máximo.")
                    Spacer(Modifier.height(4.dp))
                    Text("• Revisa fugas en tuberías y WC.")
                    Spacer(Modifier.height(4.dp))
                    Text("• Usa carga completa en la lavadora.")
                }
            },
            confirmButton = { TextButton(onClick = { showTips = false }) { Text("Entendido") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("WaterHelp 💧", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            IconButton(onClick = { showTips = true }) {
                Icon(Icons.Default.Info, "Consejos", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // 2. Tarjeta de Límite
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tu Límite Diario: ${limit.toInt()} Litros", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        label = { Text("Nuevo Límite") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        limitInput.toDoubleOrNull()?.let { viewModel.updateLimit(it); limitInput = "" }
                    }) { Text("Guardar") }
                }
            }
        }

        // 3. Selección de Fecha (CORREGIDO CON BOX)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de Registro") },
                trailingIcon = { Icon(Icons.Default.DateRange, "Calendario") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            // Este Box ahora está dentro de otro Box, así que matchParentSize funciona
            Box(Modifier.matchParentSize().clickable { dateDialog.show() })
        }

        // 4. Registrar Consumo
        Text("Registrar Consumo (Litros)", fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = litersInput,
                onValueChange = { litersInput = it },
                label = { Text("Ej. 20 (Bañarse)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                litersInput.toDoubleOrNull()?.let {
                    viewModel.addWater(it)
                    litersInput = ""
                }
            }) { Text("Añadir") }
        }

        // 5. Resumen y Mensajes
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if(isOverLimit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total del Día", fontSize = 18.sp)
                Text("${String.format("%.1f", consumption)} L", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(message, color = messageColor, fontWeight = FontWeight.Bold)
            }
        }

        // 6. Gráfica de Barras
        Text("Historial (Últimos 7 días)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top=16.dp))

        if (history.isNotEmpty()) {
            BarChart(data = history)
        } else {
            Text("No hay datos suficientes aún.", color = Color.Gray)
        }
    }
}

@Composable
fun BarChart(data: List<WaterRecord>) {
    val groupedData = data.groupBy { it.date }
        .mapValues { it.value.sumOf { r -> r.liters } }

    val maxVal = groupedData.values.maxOrNull()?.toFloat() ?: 100f

    val today = LocalDate.now().toEpochDay()
    val last7Days = (6 downTo 0).map { today - it }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        last7Days.forEach { dayEpoch ->
            val value = groupedData[dayEpoch]?.toFloat() ?: 0f

            val heightRatio = if (maxVal > 0) value / maxVal else 0f
            val barHeight = if (value > 0) heightRatio else 0.01f

            val dateLabel = LocalDate.ofEpochDay(dayEpoch)
                .format(DateTimeFormatter.ofPattern("dd/MM"))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                if (value > 0) {
                    Text(String.format("%.0f", value), fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(barHeight)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.height(4.dp))
                Text(dateLabel, fontSize = 10.sp)
            }
        }
    }
}