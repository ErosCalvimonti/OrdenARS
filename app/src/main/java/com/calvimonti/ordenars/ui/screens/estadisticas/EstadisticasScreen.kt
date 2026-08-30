package com.calvimonti.ordenars.ui.screens.estadisticas

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.calvimonti.ordenars.ui.components.GraficoBarras
import com.calvimonti.ordenars.ui.components.GraficoTorta
import com.calvimonti.ordenars.ui.components.RebanadaGrafico
import com.calvimonti.ordenars.ui.components.formatearMonto
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import com.calvimonti.ordenars.ui.theme.TextoTerciario

@Composable
fun EstadisticasScreen(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit = {},
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    val resumen by viewModel.resumenPorCategoria.collectAsStateWithLifecycle()
    val total by viewModel.total.collectAsStateWithLifecycle()

    val rebanadas = resumen.map { item ->
        RebanadaGrafico(
            etiqueta = item.categoria.nombre,
            valor = item.monto,
            color = colorDesdeHex(item.categoria.color),
            icono = item.categoria.icono
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = FondoPrincipal,
        topBar = { EncabezadoEstadisticas(onVolver = onVolver) }
    ) { padding ->
        if (rebanadas.isEmpty()) {
            EstadoVacio(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    viewModel.tituloPeriodo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
                Text(
                    formatearMonto(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario
                )
                Text(
                    "gastado en total este mes",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }

            TarjetaGrafico(titulo = "Distribución por categoría") {
                GraficoTorta(
                    datos = rebanadas,
                    modifier = Modifier.fillMaxWidth(),
                    tamanoDona = 160.dp
                )
            }

            TarjetaGrafico(titulo = "Cuánto en cada una") {
                GraficoBarras(datos = rebanadas, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun EncabezadoEstadisticas(onVolver: () -> Unit) {
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
            text = "Estadísticas",
            style = MaterialTheme.typography.headlineMedium,
            color = TextoPrimario
        )
    }
}

@Composable
private fun TarjetaGrafico(titulo: String, contenido: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FondoCard)
            .padding(16.dp)
    ) {
        Text(titulo, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        Spacer(Modifier.height(16.dp))
        contenido()
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
            Icons.Filled.PieChart,
            contentDescription = null,
            tint = TextoTerciario,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sin gastos este mes", style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
        Spacer(Modifier.height(4.dp))
        Text(
            "Cargá algún gasto para ver los gráficos",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario
        )
    }
}

private fun colorDesdeHex(hex: String): Color = Color(android.graphics.Color.parseColor(hex))
