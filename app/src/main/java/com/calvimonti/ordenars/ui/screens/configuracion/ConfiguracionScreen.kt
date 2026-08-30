package com.calvimonti.ordenars.ui.screens.configuracion

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calvimonti.ordenars.service.OrdenARSNotificationListener
import com.calvimonti.ordenars.ui.components.OrdenARSCard
import com.calvimonti.ordenars.ui.navigation.OrdenARSBottomBar
import com.calvimonti.ordenars.ui.navigation.RutasOrdenARS
import com.calvimonti.ordenars.ui.theme.Advertencia
import com.calvimonti.ordenars.ui.theme.Alerta
import com.calvimonti.ordenars.ui.theme.BorderSutil
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario

private val COLOR_POR_BILLETERA = mapOf(
    "Personal Pay" to Primario,
    "Naranja X" to Advertencia,
    "Mercado Pago" to Color(0xFF2D9CDB),
    "Santander" to Alerta,
    "BBVA" to Color(0xFF1B4F8C),
    "Galicia" to Color(0xFF8C1B1B)
)

@Composable
fun ConfiguracionScreen(
    modifier: Modifier = Modifier,
    onNavegar: (String) -> Unit = {},
    viewModel: ConfiguracionViewModel = hiltViewModel()
) {
    val contexto = LocalContext.current
    val notificacionesHabilitadas by viewModel.notificacionesHabilitadas.collectAsStateWithLifecycle()
    val cantidadCategorias by viewModel.cantidadCategorias.collectAsStateWithLifecycle()
    val billeterasDeshabilitadas by viewModel.billeterasDeshabilitadas.collectAsStateWithLifecycle()
    val archivoExportado by viewModel.archivoExportado.collectAsStateWithLifecycle()
    val historialLimpiado by viewModel.historialLimpiado.collectAsStateWithLifecycle()

    var mostrarDialogoLimpiar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // El permiso de notificaciones se otorga en Ajustes del sistema, fuera de
    // la app — cuando el usuario vuelve, hay que releerlo (ON_RESUME).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.actualizarPermisoNotificaciones()
    }

    LaunchedEffect(archivoExportado) {
        val uri = archivoExportado ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        contexto.startActivity(Intent.createChooser(intent, "Exportar gastos"))
        viewModel.limpiarArchivoExportado()
    }

    LaunchedEffect(historialLimpiado) {
        if (historialLimpiado) {
            snackbarHostState.showSnackbar("Se borró todo el historial de gastos")
            viewModel.ackHistorialLimpiado()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = FondoPrincipal,
        bottomBar = { OrdenARSBottomBar(RutasOrdenARS.CONFIGURACION, onNavegar) },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text("Configuración", style = MaterialTheme.typography.headlineMedium, color = TextoPrimario)
            }

            item {
                SeccionNotificaciones(
                    habilitadas = notificacionesHabilitadas,
                    onAbrirAjustes = {
                        contexto.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                )
            }

            item { SeccionAlertasPresupuesto() }

            item {
                SeccionBilleteras(
                    deshabilitadas = billeterasDeshabilitadas,
                    onToggle = viewModel::setBilleteraHabilitada
                )
            }

            item {
                SeccionCategorias(
                    cantidad = cantidadCategorias,
                    onClick = { onNavegar(RutasOrdenARS.CATEGORIAS) }
                )
            }

            item {
                SeccionDatos(
                    onExportar = viewModel::exportarCsv,
                    onLimpiar = { mostrarDialogoLimpiar = true }
                )
            }

            item { SeccionAcercaDe() }
        }
    }

    if (mostrarDialogoLimpiar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLimpiar = false },
            title = { Text("¿Borrar todo el historial?") },
            text = { Text("Se van a eliminar todos los gastos guardados, tanto manuales como detectados automáticamente. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.limpiarHistorial()
                    mostrarDialogoLimpiar = false
                }) {
                    Text("Borrar", color = Alerta)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoLimpiar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.bodySmall,
        color = TextoSecundario,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SeccionNotificaciones(habilitadas: Boolean, onAbrirAjustes: () -> Unit) {
    Column {
        TituloSeccion("Notificaciones automáticas")
        OrdenARSCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Primario)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Detección automática", style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
                        Text(
                            "Leer notificaciones de billeteras",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                    Switch(
                        checked = habilitadas,
                        onCheckedChange = { onAbrirAjustes() },
                        colors = SwitchDefaults.colors(checkedTrackColor = Primario)
                    )
                }
                if (!habilitadas) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Advertencia.copy(alpha = 0.15f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Advertencia, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "La detección automática está desactivada",
                            style = MaterialTheme.typography.bodySmall,
                            color = Advertencia
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                        .clickable(onClick = onAbrirAjustes)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Configurar permiso en Android", style = MaterialTheme.typography.labelMedium, color = Primario)
                }
            }
        }
    }
}

// Antes de Android 13 (API 33) las apps podían mostrar notificaciones sin
// pedir nada — a partir de ahí es un permiso runtime más, como la cámara o
// la ubicación. Por eso el chequeo siempre da "true" en versiones viejas.
private fun tienePermisoNotificaciones(contexto: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        contexto,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun SeccionAlertasPresupuesto() {
    val contexto = LocalContext.current
    var habilitado by remember { mutableStateOf(tienePermisoNotificaciones(contexto)) }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> habilitado = concedido }

    // Si el usuario lo activa/desactiva desde Ajustes y vuelve a la app, hay
    // que releer el estado real — mismo patrón que el permiso de arriba.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        habilitado = tienePermisoNotificaciones(contexto)
    }

    Column {
        TituloSeccion("Alertas de presupuesto")
        OrdenARSCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Primario)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Avisos de presupuesto", style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
                        Text(
                            "Te avisamos por notificación al 80% y al 100% de un presupuesto",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                    Switch(
                        checked = habilitado,
                        onCheckedChange = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Primario)
                    )
                }
                if (!habilitado) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Transparent)
                            .clickable {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
                                }
                                contexto.startActivity(intent)
                            }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Configurar permiso en Android", style = MaterialTheme.typography.labelMedium, color = Primario)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionBilleteras(deshabilitadas: Set<String>, onToggle: (String, Boolean) -> Unit) {
    Column {
        TituloSeccion("Billeteras")
        OrdenARSCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                val entradas = OrdenARSNotificationListener.BILLETERAS_CONOCIDAS.entries.toList()
                entradas.forEachIndexed { indice, (paquete, nombre) ->
                    FilaBilletera(
                        nombre = nombre,
                        habilitada = paquete !in deshabilitadas,
                        color = COLOR_POR_BILLETERA[nombre] ?: Primario,
                        onToggle = { habilitada -> onToggle(paquete, habilitada) }
                    )
                    if (indice != entradas.lastIndex) {
                        HorizontalDivider(color = BorderSutil, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaBilletera(nombre: String, habilitada: Boolean, color: Color, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                nombre.first().toString(),
                style = MaterialTheme.typography.bodySmall,
                color = TextoPrimario,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(nombre, style = MaterialTheme.typography.bodyLarge, color = TextoPrimario, modifier = Modifier.weight(1f))
        Switch(
            checked = habilitada,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = Primario)
        )
    }
}

@Composable
private fun SeccionCategorias(cantidad: Int, onClick: () -> Unit) {
    Column {
        TituloSeccion("Categorías")
        OrdenARSCard(modifier = Modifier.fillMaxWidth()) {
            FilaNavegable(
                icono = Icons.Filled.Edit,
                titulo = "Administrar categorías",
                subtitulo = "$cantidad categorías configuradas",
                onClick = onClick
            )
        }
    }
}

@Composable
private fun SeccionDatos(onExportar: () -> Unit, onLimpiar: () -> Unit) {
    Column {
        TituloSeccion("Datos")
        OrdenARSCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                FilaNavegable(icono = Icons.Filled.Download, titulo = "Exportar datos", onClick = onExportar)
                HorizontalDivider(color = BorderSutil, thickness = 1.dp)
                FilaNavegable(icono = Icons.Filled.DeleteOutline, titulo = "Limpiar historial", onClick = onLimpiar)
            }
        }
    }
}

@Composable
private fun FilaNavegable(
    icono: ImageVector,
    titulo: String,
    subtitulo: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = Primario)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
            if (subtitulo != null) {
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextoSecundario)
    }
}

@Composable
private fun SeccionAcercaDe() {
    Column {
        TituloSeccion("Acerca de")
        OrdenARSCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("OrdenARS v1.0.0", style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Hecho con Kotlin y Jetpack Compose",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
        }
    }
}
