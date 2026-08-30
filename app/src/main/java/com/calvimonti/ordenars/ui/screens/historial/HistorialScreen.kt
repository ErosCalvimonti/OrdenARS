package com.calvimonti.ordenars.ui.screens.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.FiltroHistorial
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.OrigenGasto
import com.calvimonti.ordenars.ui.components.formatearMonto
import com.calvimonti.ordenars.ui.navigation.OrdenARSBottomBar
import com.calvimonti.ordenars.ui.navigation.RutasOrdenARS
import com.calvimonti.ordenars.ui.theme.Alerta
import com.calvimonti.ordenars.ui.theme.BorderSutil
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.FondoSecundario
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import com.calvimonti.ordenars.ui.theme.TextoTerciario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistorialScreen(
    modifier: Modifier = Modifier,
    onNavegar: (String) -> Unit = {},
    onGastoClick: (Gasto) -> Unit = {},
    viewModel: HistorialViewModel = hiltViewModel()
) {
    val gastosFiltrados by viewModel.gastosFiltrados.collectAsStateWithLifecycle()
    val totalFiltrado by viewModel.totalFiltrado.collectAsStateWithLifecycle()
    val cantidadFiltrada by viewModel.cantidadFiltrada.collectAsStateWithLifecycle()
    val filtroActivo by viewModel.filtroActivo.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = FondoPrincipal,
        bottomBar = {
            Column {
                ResumenFlotante(cantidad = cantidadFiltrada, total = totalFiltrado)
                OrdenARSBottomBar(RutasOrdenARS.HISTORIAL, onNavegar)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Historial", style = MaterialTheme.typography.headlineMedium, color = TextoPrimario)
                Text(
                    text = "${formatearMonto(totalFiltrado)} en $cantidadFiltrada gastos",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
                Spacer(Modifier.height(16.dp))
                BarraBusqueda(busqueda = busqueda, onBusquedaChange = viewModel::buscar)
                Spacer(Modifier.height(16.dp))
                FiltrosRow(
                    filtroActivo = filtroActivo,
                    categorias = categorias,
                    onFiltrarPorPeriodo = viewModel::filtrarPorPeriodo,
                    onFiltrarPorCategoria = viewModel::filtrarPorCategoria
                )
            }

            if (gastosFiltrados.isEmpty()) {
                EstadoVacio(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    gastosFiltrados.forEach { (etiqueta, gastos) ->
                        stickyHeader { EncabezadoFecha(etiqueta) }
                        items(gastos, key = { it.id }) { gasto ->
                            FilaGastoHistorial(
                                gasto = gasto,
                                categoria = categorias.find { it.nombre == gasto.categoria },
                                onClick = { onGastoClick(gasto) },
                                onEliminar = { viewModel.eliminarGasto(gasto) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraBusqueda(busqueda: String, onBusquedaChange: (String) -> Unit) {
    TextField(
        value = busqueda,
        onValueChange = onBusquedaChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar gasto...", color = TextoTerciario) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextoSecundario) },
        singleLine = true,
        shape = RoundedCornerShape(percent = 50),
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
}

@Composable
private fun FiltrosRow(
    filtroActivo: FiltroHistorial,
    categorias: List<Categoria>,
    onFiltrarPorPeriodo: (FiltroHistorial) -> Unit,
    onFiltrarPorCategoria: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            ChipFiltro(
                texto = "Todos",
                seleccionado = filtroActivo == FiltroHistorial.Todos,
                onClick = { onFiltrarPorPeriodo(FiltroHistorial.Todos) }
            )
        }
        item {
            ChipFiltro(
                texto = "Esta semana",
                seleccionado = filtroActivo == FiltroHistorial.EstaSemana,
                onClick = { onFiltrarPorPeriodo(FiltroHistorial.EstaSemana) }
            )
        }
        item {
            ChipFiltro(
                texto = "Este mes",
                seleccionado = filtroActivo == FiltroHistorial.EsteMes,
                onClick = { onFiltrarPorPeriodo(FiltroHistorial.EsteMes) }
            )
        }
        item {
            ChipFiltro(
                texto = "Últimos 3 meses",
                seleccionado = filtroActivo == FiltroHistorial.Ultimos3Meses,
                onClick = { onFiltrarPorPeriodo(FiltroHistorial.Ultimos3Meses) }
            )
        }
        items(categorias, key = { it.nombre }) { categoria ->
            ChipFiltro(
                texto = "${categoria.icono} ${categoria.nombre}",
                seleccionado = filtroActivo == FiltroHistorial.Categoria(categoria.nombre),
                onClick = { onFiltrarPorCategoria(categoria.nombre) }
            )
        }
    }
}

@Composable
private fun ChipFiltro(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (seleccionado) Primario else FondoSecundario)
            .border(
                width = if (seleccionado) 0.dp else 1.dp,
                color = BorderSutil,
                shape = RoundedCornerShape(percent = 50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (seleccionado) TextoPrimario else TextoSecundario
        )
    }
}

@Composable
private fun EncabezadoFecha(etiqueta: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FondoPrincipal)
            .padding(vertical = 8.dp)
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSutil))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaGastoHistorial(
    gasto: Gasto,
    categoria: Categoria?,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    val estadoSwipe = rememberSwipeToDismissBoxState(
        confirmValueChange = { valor ->
            if (valor == SwipeToDismissBoxValue.EndToStart) {
                onEliminar()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = estadoSwipe,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Alerta),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = TextoPrimario,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FondoPrincipal)
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(categoria?.color?.let { colorDesdeHex(it) } ?: FondoSecundario),
                contentAlignment = Alignment.Center
            ) {
                Text(categoria?.icono ?: "📦", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.descripcion, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                Text(
                    text = "${gasto.nombreBilletera.ifBlank { gasto.categoria }} · ${formatearHora(gasto.fecha)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatearMonto(gasto.monto),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Alerta
                )
                Text(
                    text = if (gasto.origen == OrigenGasto.NOTIFICACION) "Auto" else "Manual",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoTerciario
                )
            }
        }
    }
}

@Composable
private fun EstadoVacio(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = TextoTerciario,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sin resultados", style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
        Spacer(Modifier.height(4.dp))
        Text("Probá con otros filtros", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
    }
}

@Composable
private fun ResumenFlotante(cantidad: Int, total: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FondoCard)
            .border(1.dp, Primario, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$cantidad ${if (cantidad == 1) "gasto encontrado" else "gastos encontrados"}",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario
        )
        Text(
            text = formatearMonto(total),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario
        )
    }
}

private fun colorDesdeHex(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

private fun formatearHora(fechaMillis: Long): String =
    SimpleDateFormat("HH:mm", Locale("es", "AR")).format(Date(fechaMillis))
