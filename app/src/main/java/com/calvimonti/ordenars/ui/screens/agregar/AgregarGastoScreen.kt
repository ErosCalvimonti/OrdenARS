package com.calvimonti.ordenars.ui.screens.agregar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.ui.components.GradientButton
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.Secundario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import com.calvimonti.ordenars.ui.theme.TextoTerciario
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgregarGastoScreen(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit = {},
    viewModel: AgregarGastoViewModel = hiltViewModel()
) {
    val monto by viewModel.monto.collectAsStateWithLifecycle()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsStateWithLifecycle()
    val descripcion by viewModel.descripcion.collectAsStateWithLifecycle()
    val fecha by viewModel.fecha.collectAsStateWithLifecycle()
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    val formularioValido by viewModel.formularioValido.collectAsStateWithLifecycle()
    val gastoGuardado by viewModel.gastoGuardado.collectAsStateWithLifecycle()

    LaunchedEffect(gastoGuardado) {
        if (gastoGuardado) {
            delay(700)
            onVolver()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = FondoPrincipal,
            topBar = {
                EncabezadoAgregarGasto(
                    onVolver = onVolver,
                    guardarHabilitado = formularioValido,
                    onGuardar = viewModel::guardarGasto
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                CampoMonto(monto = monto, onMontoChange = viewModel::onMontoChange)
                Spacer(Modifier.height(28.dp))
                SelectorCategoria(
                    categorias = categorias,
                    seleccionada = categoriaSeleccionada,
                    onSeleccionar = viewModel::onCategoriaSeleccionada
                )
                Spacer(Modifier.height(24.dp))
                CampoDescripcion(descripcion = descripcion, onDescripcionChange = viewModel::onDescripcionChange)
                Spacer(Modifier.height(24.dp))
                SelectorFecha(fecha = fecha, onFechaChange = viewModel::onFechaChange)
                Spacer(Modifier.height(32.dp))
                GradientButton(
                    texto = "Guardar gasto",
                    onClick = viewModel::guardarGasto,
                    habilitado = formularioValido,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        AnimatedVisibility(
            visible = gastoGuardado,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            ConfirmacionGuardado()
        }
    }
}

@Composable
private fun EncabezadoAgregarGasto(
    onVolver: () -> Unit,
    guardarHabilitado: Boolean,
    onGuardar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(FondoPrincipal)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = onVolver, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoPrimario)
        }
        Text(
            text = "Nuevo gasto",
            style = MaterialTheme.typography.headlineMedium,
            color = TextoPrimario,
            modifier = Modifier.align(Alignment.Center)
        )
        TextButton(
            onClick = onGuardar,
            enabled = guardarHabilitado,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = "Guardar",
                color = if (guardarHabilitado) Primario else TextoTerciario,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CampoMonto(monto: String, onMontoChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Primario.copy(alpha = 0.125f), Color.Transparent))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.displayLarge,
                    color = Primario,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                BasicTextField(
                    value = monto,
                    onValueChange = onMontoChange,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .width(IntrinsicSize.Min),
                    textStyle = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, color = TextoPrimario),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Primario),
                    decorationBox = { campoInterno ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (monto.isEmpty()) {
                                Text("0", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = TextoTerciario)
                            }
                            campoInterno()
                        }
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Pesos argentinos", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        }
    }
}

@Composable
private fun SelectorCategoria(
    categorias: List<Categoria>,
    seleccionada: String?,
    onSeleccionar: (String) -> Unit
) {
    Column {
        Text("Categoría", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        Spacer(Modifier.height(12.dp))
        categorias.chunked(3).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { categoria ->
                    ItemCategoria(
                        categoria = categoria,
                        seleccionada = categoria.nombre == seleccionada,
                        onClick = { onSeleccionar(categoria.nombre) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - fila.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ItemCategoria(
    categoria: Categoria,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (seleccionada) Primario.copy(alpha = 0.15f) else FondoCard)
                .border(
                    width = if (seleccionada) 2.dp else 0.dp,
                    color = if (seleccionada) Primario else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(categoria.icono, style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = categoria.nombre,
            style = MaterialTheme.typography.bodySmall,
            color = TextoPrimario,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CampoDescripcion(descripcion: String, onDescripcionChange: (String) -> Unit) {
    Column {
        Text("Descripción", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        Spacer(Modifier.height(8.dp))
        TextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("¿En qué gastaste?", color = TextoTerciario) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FondoCard,
                unfocusedContainerColor = FondoCard,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TextoPrimario,
                unfocusedTextColor = TextoPrimario,
                cursorColor = Primario
            )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${descripcion.length}/100",
            style = MaterialTheme.typography.bodySmall,
            color = TextoTerciario,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(fecha: Long, onFechaChange: (Long) -> Unit) {
    var mostrarSelector by remember { mutableStateOf(false) }

    Column {
        Text("Fecha", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FondoCard)
                .clickable { mostrarSelector = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Primario, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(formatearFechaLarga(fecha), color = TextoPrimario, style = MaterialTheme.typography.bodyLarge)
        }
    }

    if (mostrarSelector) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = fecha)
        DatePickerDialog(
            onDismissRequest = { mostrarSelector = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let(onFechaChange)
                    mostrarSelector = false
                }) { Text("Aceptar", color = Primario) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelector = false }) { Text("Cancelar", color = TextoSecundario) }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}

@Composable
private fun ConfirmacionGuardado() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPrincipal.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Secundario),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = TextoPrimario, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Gasto guardado",
                style = MaterialTheme.typography.titleMedium,
                color = TextoPrimario
            )
        }
    }
}

private fun formatearFechaLarga(fechaMillis: Long): String =
    SimpleDateFormat("d 'de' MMMM", Locale("es", "AR")).format(Date(fechaMillis))
