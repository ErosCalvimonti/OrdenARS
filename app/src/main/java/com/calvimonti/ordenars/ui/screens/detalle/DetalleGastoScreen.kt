package com.calvimonti.ordenars.ui.screens.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calvimonti.ordenars.domain.model.GastoPorCategoria
import com.calvimonti.ordenars.domain.model.OrigenGasto
import com.calvimonti.ordenars.ui.components.colorSemaforoPresupuesto
import com.calvimonti.ordenars.ui.components.formatearMonto
import com.calvimonti.ordenars.ui.theme.Alerta
import com.calvimonti.ordenars.ui.theme.BorderSutil
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.FondoSecundario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DetalleGastoScreen(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit = {},
    viewModel: DetalleGastoViewModel = hiltViewModel()
) {
    val gasto by viewModel.gasto.collectAsStateWithLifecycle()
    val categoria by viewModel.categoria.collectAsStateWithLifecycle()
    val presupuestoDeCategoria by viewModel.presupuestoDeCategoria.collectAsStateWithLifecycle()
    var mostrarConfirmarBorrado by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = FondoPrincipal,
        topBar = { EncabezadoDetalle(onVolver = onVolver) }
    ) { padding ->
        val gastoActual = gasto
        if (gastoActual == null) {
            // Puede pasar por un id inválido, o justo después de borrar el
            // gasto (el flow emite null antes de que `onVolver` saque la
            // pantalla del stack) — en cualquier caso, un placeholder alcanza.
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No se encontró el gasto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = formatearMonto(gastoActual.monto),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Alerta
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = gastoActual.descripcion,
                style = MaterialTheme.typography.titleMedium,
                color = TextoPrimario
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FondoCard)
            ) {
                FilaDetalle(etiqueta = "Categoría") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(categoria?.color?.let(::colorDesdeHex) ?: FondoSecundario),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(categoria?.icono ?: "📦", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            gastoActual.categoria,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoPrimario
                        )
                    }
                }
                Divisor()
                FilaDetalle(etiqueta = "Fecha") {
                    Text(
                        formatearFechaLarga(gastoActual.fecha),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoPrimario
                    )
                }
                Divisor()
                FilaDetalle(etiqueta = "Origen") {
                    Text(
                        text = when (gastoActual.origen) {
                            OrigenGasto.NOTIFICACION ->
                                "Detectado automáticamente" +
                                    gastoActual.nombreBilletera.takeIf { it.isNotBlank() }
                                        ?.let { " · $it" }.orEmpty()
                            OrigenGasto.MANUAL -> "Cargado a mano"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoPrimario
                    )
                }
                if (gastoActual.textoOriginal.isNotBlank()) {
                    Divisor()
                    FilaDetalle(etiqueta = "Notificación original") {
                        Text(
                            gastoActual.textoOriginal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoPrimario
                        )
                    }
                }
            }

            presupuestoDeCategoria?.let { item ->
                Spacer(Modifier.height(16.dp))
                TarjetaPresupuestoGasto(item = item, montoDelGasto = gastoActual.monto)
            }

            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { mostrarConfirmarBorrado = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = null,
                    tint = Alerta,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Eliminar gasto", style = MaterialTheme.typography.labelLarge, color = Alerta)
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (mostrarConfirmarBorrado) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarBorrado = false },
            title = { Text("¿Eliminar este gasto?") },
            text = {
                Text(
                    "Se borra de forma permanente. Si estaba contando para un " +
                        "presupuesto, ese total se recalcula."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmarBorrado = false
                    viewModel.eliminar(onVolver)
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

@Composable
private fun EncabezadoDetalle(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FondoPrincipal)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolver) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoPrimario)
        }
        Text(
            text = "Detalle del gasto",
            style = MaterialTheme.typography.headlineSmall,
            color = TextoPrimario
        )
    }
}

@Composable
private fun FilaDetalle(etiqueta: String, contenido: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        Spacer(Modifier.height(6.dp))
        contenido()
    }
}

@Composable
private fun TarjetaPresupuestoGasto(item: GastoPorCategoria, montoDelGasto: Double) {
    val montoMaximo = item.montoPresupuestado ?: return
    val porcentaje = item.porcentajeUsado
    val color = colorSemaforoPresupuesto(porcentaje)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FondoCard)
            .padding(16.dp)
    ) {
        Text(
            "Presupuesto de ${item.categoria.nombre}",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${formatearMonto(item.montoGastado)} de ${formatearMonto(montoMaximo)}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextoPrimario
            )
            Text(
                "${(porcentaje * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { porcentaje.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = FondoSecundario
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Este gasto es el ${(montoDelGasto / montoMaximo * 100).roundToInt()}% del presupuesto del mes",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario
        )
    }
}

@Composable
private fun Divisor() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSutil))
}

private fun colorDesdeHex(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

private fun formatearFechaLarga(fechaMillis: Long): String =
    SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy · HH:mm", Locale("es", "AR"))
        .format(Date(fechaMillis))
        .replaceFirstChar { it.uppercase(Locale("es", "AR")) }
