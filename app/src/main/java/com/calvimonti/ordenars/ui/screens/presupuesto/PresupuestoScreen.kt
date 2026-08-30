package com.calvimonti.ordenars.ui.screens.presupuesto

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.PresupuestoConGasto
import com.calvimonti.ordenars.ui.components.AvisoPresupuesto
import com.calvimonti.ordenars.ui.components.GradientButton
import com.calvimonti.ordenars.ui.components.formatearMonto
import com.calvimonti.ordenars.ui.navigation.OrdenARSBottomBar
import com.calvimonti.ordenars.ui.navigation.RutasOrdenARS
import com.calvimonti.ordenars.ui.theme.Advertencia
import com.calvimonti.ordenars.ui.theme.Alerta
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.FondoSecundario
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.Secundario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import com.calvimonti.ordenars.ui.theme.TextoTerciario
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestoScreen(
    modifier: Modifier = Modifier,
    onNavegar: (String) -> Unit = {},
    viewModel: PresupuestoViewModel = hiltViewModel()
) {
    val presupuestos by viewModel.presupuestosPorCategoria.collectAsStateWithLifecycle()
    val totalPresupuestado by viewModel.totalPresupuestado.collectAsStateWithLifecycle()
    val totalGastado by viewModel.totalGastado.collectAsStateWithLifecycle()
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()

    var presupuestoEnEdicion by remember { mutableStateOf<PresupuestoConGasto?>(null) }
    var mostrarHoja by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = FondoPrincipal,
        bottomBar = { OrdenARSBottomBar(RutasOrdenARS.PRESUPUESTO, onNavegar) }
    ) { padding ->
        if (presupuestos.isEmpty()) {
            EstadoVacio(
                modifier = Modifier.fillMaxSize().padding(padding),
                onEmpezar = {
                    presupuestoEnEdicion = null
                    mostrarHoja = true
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Encabezado(
                        subtitulo = viewModel.tituloPeriodo,
                        onAgregar = {
                            presupuestoEnEdicion = null
                            mostrarHoja = true
                        }
                    )
                }
                item {
                    ResumenGeneral(total = totalPresupuestado, gastado = totalGastado)
                }
                items(presupuestos, key = { it.categoria.nombre }) { item ->
                    CardPresupuestoCategoria(
                        item = item,
                        onClick = {
                            presupuestoEnEdicion = item
                            mostrarHoja = true
                        }
                    )
                }
            }
        }
    }

    if (mostrarHoja) {
        HojaAgregarPresupuesto(
            categorias = categorias,
            categoriaInicial = presupuestoEnEdicion?.categoria?.nombre,
            montoInicial = presupuestoEnEdicion?.montoMaximo,
            periodo = viewModel.tituloPeriodo,
            onGuardar = { categoria, monto ->
                if (presupuestoEnEdicion != null) {
                    viewModel.editarPresupuesto(categoria, monto)
                } else {
                    viewModel.agregarPresupuesto(categoria, monto)
                }
                mostrarHoja = false
            },
            onEliminar = presupuestoEnEdicion?.let { enEdicion ->
                {
                    viewModel.eliminarPresupuesto(enEdicion.categoria.nombre)
                    mostrarHoja = false
                }
            },
            onDismiss = { mostrarHoja = false }
        )
    }
}

@Composable
private fun Encabezado(subtitulo: String, onAgregar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Presupuestos", style = MaterialTheme.typography.headlineMedium, color = TextoPrimario)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        }
        IconButton(
            onClick = onAgregar,
            modifier = Modifier
                .clip(CircleShape)
                .background(Primario)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar presupuesto", tint = TextoPrimario)
        }
    }
}

@Composable
private fun ResumenGeneral(total: Double, gastado: Double) {
    val disponible = total - gastado
    val progreso = if (total > 0) (gastado / total).toFloat().coerceIn(0f, 1f) else 0f
    val progresoAnimado by animateFloatAsState(targetValue = progreso, label = "progresoGeneral")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(FondoCard, FondoSecundario)))
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricaResumen("Total presupuestado", total, TextoPrimario, Modifier.weight(1f))
            MetricaResumen("Gastado", gastado, Alerta, Modifier.weight(1f))
            MetricaResumen("Disponible", disponible, Secundario, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progresoAnimado },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = if (progreso > 0.8f) Alerta else Secundario,
            trackColor = FondoSecundario
        )
    }
}

@Composable
private fun MetricaResumen(etiqueta: String, monto: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        Spacer(Modifier.height(4.dp))
        Text(formatearMonto(monto), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun CardPresupuestoCategoria(item: PresupuestoConGasto, onClick: () -> Unit) {
    val progreso = item.porcentajeUsado.coerceIn(0f, 1f)
    val progresoAnimado by animateFloatAsState(targetValue = progreso, label = "progresoCategoria")
    val colorBarra = when {
        item.porcentajeUsado > 0.8f -> Alerta
        item.porcentajeUsado > 0.6f -> Advertencia
        else -> Secundario
    }

    val transicionPulso = rememberInfiniteTransition(label = "pulso")
    val alphaPulso by if (item.porcentajeUsado > 0.8f) {
        transicionPulso.animateFloat(
            initialValue = 1f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alphaPulso"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FondoCard)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorDesdeHex(item.categoria.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.categoria.icono, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = item.categoria.nombre,
                style = MaterialTheme.typography.bodyLarge,
                color = TextoPrimario,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatearMonto(item.montoGastado)} / ${formatearMonto(item.montoMaximo)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { progresoAnimado },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .alpha(alphaPulso),
                color = colorBarra,
                trackColor = FondoSecundario
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${(item.porcentajeUsado * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
        AvisoPresupuesto(porcentajeUsado = item.porcentajeUsado)
    }
}

@Composable
private fun EstadoVacio(modifier: Modifier = Modifier, onEmpezar: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.TrackChanges,
            contentDescription = null,
            tint = TextoTerciario,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sin presupuestos", style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
        Spacer(Modifier.height(4.dp))
        Text(
            "Configurá límites para cada categoría",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario
        )
        Spacer(Modifier.height(20.dp))
        GradientButton(texto = "Empezar", onClick = onEmpezar, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HojaAgregarPresupuesto(
    categorias: List<Categoria>,
    categoriaInicial: String?,
    montoInicial: Double?,
    periodo: String,
    onGuardar: (categoria: String, monto: Double) -> Unit,
    onEliminar: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState()
    var categoriaSeleccionada by remember { mutableStateOf(categoriaInicial) }
    var monto by remember { mutableStateOf(montoInicial?.let { it.toInt().toString() } ?: "") }
    var mostrarConfirmarBorrado by remember { mutableStateOf(false) }

    val teclado = LocalSoftwareKeyboardController.current
    val focoActual = LocalFocusManager.current
    val tecladoVisible = WindowInsets.isImeVisible
    BackHandler(enabled = tecladoVisible) {
        teclado?.hide()
        focoActual.clearFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = estadoHoja,
        containerColor = FondoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = if (categoriaInicial != null) "Editar presupuesto" else "Nuevo presupuesto",
                style = MaterialTheme.typography.headlineMedium,
                color = TextoPrimario
            )
            Spacer(Modifier.height(20.dp))

            Text("Categoría", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categorias, key = { it.nombre }) { categoria ->
                    val seleccionada = categoria.nombre == categoriaSeleccionada
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(if (seleccionada) Primario else FondoSecundario)
                            .clickable { categoriaSeleccionada = categoria.nombre }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${categoria.icono} ${categoria.nombre}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (seleccionada) TextoPrimario else TextoSecundario
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Monto máximo", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FondoSecundario)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$", style = MaterialTheme.typography.bodyLarge, color = Primario, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                BasicTextField(
                    value = monto,
                    onValueChange = { nuevo -> monto = nuevo.filter { it.isDigit() }.take(9) },
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoPrimario),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Primario),
                    decorationBox = { campoInterno ->
                        if (monto.isEmpty()) {
                            Text("0", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoTerciario)
                        }
                        campoInterno()
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Mes", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            Spacer(Modifier.height(8.dp))
            Text(periodo, style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)

            Spacer(Modifier.height(28.dp))
            val montoDouble = monto.toDoubleOrNull() ?: 0.0
            GradientButton(
                texto = "Guardar",
                habilitado = categoriaSeleccionada != null && montoDouble > 0.0,
                onClick = {
                    val categoria = categoriaSeleccionada
                    if (categoria != null && montoDouble > 0.0) {
                        onGuardar(categoria, montoDouble)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (onEliminar != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { mostrarConfirmarBorrado = true }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Eliminar presupuesto", style = MaterialTheme.typography.labelMedium, color = Alerta)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (mostrarConfirmarBorrado && onEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarBorrado = false },
            title = { Text("¿Eliminar el presupuesto de \"${categoriaInicial.orEmpty()}\"?") },
            text = {
                Text(
                    "Se borra el límite de gasto de esta categoría para $periodo. " +
                        "Los gastos que ya cargaste no se tocan."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmarBorrado = false
                    onEliminar()
                }) {
                    Text("Eliminar", color = Alerta)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun colorDesdeHex(hex: String): Color = Color(android.graphics.Color.parseColor(hex))
