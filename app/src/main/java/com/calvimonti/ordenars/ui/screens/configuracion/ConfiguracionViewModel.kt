package com.calvimonti.ordenars.ui.screens.configuracion

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    @ApplicationContext private val contexto: Context,
    private val gastoRepository: GastoRepository,
    categoriaRepository: CategoriaRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val _notificacionesHabilitadas = MutableStateFlow(hayPermisoDeNotificaciones())
    val notificacionesHabilitadas: StateFlow<Boolean> = _notificacionesHabilitadas.asStateFlow()

    val cantidadCategorias: StateFlow<Int> = categoriaRepository.obtenerTodas()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val billeterasDeshabilitadas: StateFlow<Set<String>> = preferenciasRepository.billeterasDeshabilitadas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _archivoExportado = MutableStateFlow<Uri?>(null)
    val archivoExportado: StateFlow<Uri?> = _archivoExportado.asStateFlow()

    private val _historialLimpiado = MutableStateFlow(false)
    val historialLimpiado: StateFlow<Boolean> = _historialLimpiado.asStateFlow()

    /** Hay que llamarlo al volver a la pantalla: el permiso se otorga en Ajustes del sistema, no acá. */
    fun actualizarPermisoNotificaciones() {
        _notificacionesHabilitadas.value = hayPermisoDeNotificaciones()
    }

    private fun hayPermisoDeNotificaciones(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(contexto).contains(contexto.packageName)

    fun setBilleteraHabilitada(paqueteBilletera: String, habilitada: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.setBilleteraHabilitada(paqueteBilletera, habilitada)
        }
    }

    fun exportarCsv() {
        viewModelScope.launch {
            val gastos = gastoRepository.obtenerTodos().first()
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR"))

            val csv = buildString {
                appendLine("Fecha,Monto,Categoria,Descripcion,Origen,Billetera")
                gastos.forEach { gasto ->
                    appendLine(
                        listOf(
                            formatoFecha.format(Date(gasto.fecha)),
                            gasto.monto.toString(),
                            gasto.categoria,
                            gasto.descripcion.reemplazarComasYSaltos(),
                            gasto.origen.name,
                            gasto.nombreBilletera
                        ).joinToString(",")
                    )
                }
            }

            val carpeta = File(contexto.getExternalFilesDir("exportaciones"), "").apply { mkdirs() }
            val archivo = File(carpeta, "ordenars_gastos_${System.currentTimeMillis()}.csv")
            archivo.writeText(csv)

            _archivoExportado.value = FileProvider.getUriForFile(
                contexto,
                "${contexto.packageName}.fileprovider",
                archivo
            )
        }
    }

    fun limpiarArchivoExportado() {
        _archivoExportado.value = null
    }

    fun limpiarHistorial() {
        viewModelScope.launch {
            gastoRepository.eliminarTodos()
            _historialLimpiado.value = true
        }
    }

    fun ackHistorialLimpiado() {
        _historialLimpiado.value = false
    }
}

// Una descripción con comas o saltos de línea rompería las columnas del CSV.
private fun String.reemplazarComasYSaltos(): String = replace(",", ";").replace("\n", " ")
