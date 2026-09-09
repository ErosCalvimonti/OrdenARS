package com.calvimonti.ordenars.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.GastoPorCategoria
import com.calvimonti.ordenars.domain.model.OrigenGasto
import com.calvimonti.ordenars.ui.components.AvisoPresupuesto
import com.calvimonti.ordenars.ui.components.CategoriaChip
import com.calvimonti.ordenars.ui.components.GraficoTorta
import com.calvimonti.ordenars.ui.components.PuntoEstadoPresupuesto
import com.calvimonti.ordenars.ui.components.RebanadaGrafico
import com.calvimonti.ordenars.ui.components.SeccionTitulo
import com.calvimonti.ordenars.ui.components.formatearMonto
import com.calvimonti.ordenars.ui.navigation.OrdenARSBottomBar
import com.calvimonti.ordenars.ui.navigation.RutasOrdenARS
import com.calvimonti.ordenars.ui.theme.Advertencia
import com.calvimonti.ordenars.ui.theme.Alerta
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.FondoSecundario
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.PrimarioClaro
import com.calvimonti.ordenars.ui.theme.Secundario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onNavegar: (String) -> Unit = {},
    onVerTodoPresupuesto: () -> Unit = {},
    onVerTodoMovimientos: () -> Unit = {},
    onVerEstadisticas: () -> Unit = {},
    onClickConfiguracion: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val totalMesActual by viewModel.totalMesActual.collectAsStateWithLifecycle()
    val gastosPorCategoria by viewModel.gastosPorCategoria.collectAsStateWithLifecycle()
    val resumenPorCategoria by viewModel.resumenPorCategoria.collectAsStateWithLifecycle()
    val ultimosGastos by viewModel.ultimosGastos.collectAsStateWithLifecycle()
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    val categoriaPorNombre = remember(categorias) { categorias.associateBy { it.nombre } }
    val porcentajePresupuesto by viewModel.porcentajePresupuestoPorCategoria.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = FondoPrincipal,
        bottomBar = { OrdenARSBottomBar(RutasOrdenARS.DASHBOARD, onNavegar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(FondoPrincipal, FondoCard))),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { EncabezadoDashboard(onClickConfiguracion) }

            item { CardTotalMes(total = totalMesActual, mes = viewModel.mesActual) }

            if (resumenPorCategoria.isNotEmpty()) {
                item {
                    SeccionTitulo(
                        titulo = "Gastos por categoría",
                        textoAccion = "Ver todo",
                        onAccionClick = onVerEstadisticas
                    )
                }
                item {
                    GraficoTorta(
                        datos = resumenPorCategoria.map { resumen ->
                            RebanadaGrafico(
                                etiqueta = resumen.categoria.nombre,
                                valor = resumen.monto,
                                color = colorDesdeHex(resumen.categoria.color),
                                icono = resumen.categoria.icono
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        tamanoDona = 130.dp,
                        maxRebanadas = 5
                    )
                }
            }

            item {
                SeccionTitulo(
                    titulo = "Presupuesto",
                    textoAccion = "Ver todo",
                    onAccionClick = onVerTodoPresupuesto
                )
            }
            if (gastosPorCategoria.isEmpty()) {
                item {
                    Text(
                        text = "Todavía no hay gastos categorizados este mes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            } else {
                items(gastosPorCategoria, key = { it.categoria.nombre }) { item ->
                    FilaPresupuestoCategoria(item)
                }
            }

            item {
                SeccionTitulo(
                    titulo = "Últimos movimientos",
                    textoAccion = "Ver todo",
                    onAccionClick = onVerTodoMovimientos
                )
            }
            if (ultimosGastos.isEmpty()) {
                item {
                    Text(
                        text = "Todavía no registraste ningún gasto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            } else {
                items(ultimosGastos, key = { it.id }) { gasto ->
                    FilaGasto(
                        gasto = gasto,
                        categoria = categoriaPorNombre[gasto.categoria],
                        porcentajePresupuesto = porcentajePresupuesto[gasto.categoria]
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoDashboard(onClickConfiguracion: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                "Tus ARS en orden",
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = Brush.linearGradient(listOf(Primario, Secundario))
                )
            )
        }
        IconButton(onClick = onClickConfiguracion) {
            Icon(Icons.Filled.Settings, contentDescription = "Configuración", tint = TextoPrimario)
        }
    }
}

@Composable
private fun CardTotalMes(total: Double, mes: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Primario,
                spotColor = Primario
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Primario, PrimarioClaro)))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gastado en $mes",
                style = MaterialTheme.typography.bodySmall,
                color = TextoPrimario.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatearMonto(total),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // La app todavía no registra ingresos, solo gastos — placeholder en $0.
                ColumnaIngresoGasto(
                    etiqueta = "Ingresos",
                    monto = 0.0,
                    icono = Icons.Filled.ArrowUpward,
                    colorFlecha = Secundario
                )
                ColumnaIngresoGasto(
                    etiqueta = "Gastos",
                    monto = total,
                    icono = Icons.Filled.ArrowDownward,
                    colorFlecha = Alerta
                )
            }
        }
    }
}

@Composable
private fun ColumnaIngresoGasto(etiqueta: String, monto: Double, icono: ImageVector, colorFlecha: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = colorFlecha, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = TextoPrimario.copy(alpha = 0.8f))
        }
        Text(
            text = formatearMonto(monto),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextoPrimario
        )
    }
}

@Composable
private fun FilaPresupuestoCategoria(item: GastoPorCategoria) {
    val progreso = item.porcentajeUsado.coerceIn(0f, 1f)
    val colorBarra = when {
        item.porcentajeUsado > 0.8f -> Alerta
        item.porcentajeUsado > 0.6f -> Advertencia
        else -> Secundario
    }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colorDesdeHex(item.categoria.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.categoria.icono, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = item.categoria.nombre,
                style = MaterialTheme.typography.bodyLarge,
                color = TextoPrimario,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(item.porcentajeUsado * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = colorBarra,
            trackColor = FondoSecundario
        )
        AvisoPresupuesto(porcentajeUsado = item.porcentajeUsado)
    }
}

@Composable
private fun FilaGasto(gasto: Gasto, categoria: Categoria?, porcentajePresupuesto: Float? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(categoria?.color?.let { colorDesdeHex(it) } ?: FondoSecundario),
                contentAlignment = Alignment.Center
            ) {
                Text(categoria?.icono ?: "📦", style = MaterialTheme.typography.bodyLarge)
            }
            if (porcentajePresupuesto != null) {
                PuntoEstadoPresupuesto(
                    porcentajeUsado = porcentajePresupuesto,
                    colorFondo = FondoPrincipal
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(gasto.descripcion, style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
                if (gasto.origen == OrigenGasto.NOTIFICACION) {
                    Spacer(Modifier.width(6.dp))
                    CategoriaChip(categoria = "Auto", color = Primario)
                }
            }
            Text(
                text = gasto.nombreBilletera.ifBlank { gasto.categoria },
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatearMonto(gasto.monto),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Alerta
            )
            Text(formatearFechaCorta(gasto.fecha), style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        }
    }
}

private fun colorDesdeHex(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

private fun formatearFechaCorta(fechaMillis: Long): String =
    SimpleDateFormat("d MMM", Locale("es", "AR")).format(Date(fechaMillis))
