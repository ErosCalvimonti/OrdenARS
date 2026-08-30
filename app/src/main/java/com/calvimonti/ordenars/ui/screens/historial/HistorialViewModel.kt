package com.calvimonti.ordenars.ui.screens.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.FiltroHistorial
import com.calvimonti.ordenars.domain.model.Gasto
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val gastoRepository: GastoRepository,
    categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val _filtroActivo = MutableStateFlow<FiltroHistorial>(FiltroHistorial.Todos)
    val filtroActivo: StateFlow<FiltroHistorial> = _filtroActivo.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val gastosFiltradosLista: StateFlow<List<Gasto>> = combine(
        gastoRepository.obtenerTodos(),
        _filtroActivo,
        _busqueda
    ) { gastos, filtro, texto ->
        val porFiltro = when (filtro) {
            FiltroHistorial.Todos -> gastos
            FiltroHistorial.EstaSemana -> gastos.filter { it.fecha >= inicioDeHace(Calendar.DAY_OF_YEAR, -7) }
            FiltroHistorial.EsteMes -> gastos.filter { it.fecha >= inicioDeHace(Calendar.MONTH, 0, diaUnoDelMes = true) }
            FiltroHistorial.Ultimos3Meses -> gastos.filter { it.fecha >= inicioDeHace(Calendar.MONTH, -3) }
            is FiltroHistorial.Categoria -> gastos.filter { it.categoria == filtro.nombre }
        }
        if (texto.isBlank()) {
            porFiltro
        } else {
            porFiltro.filter { it.descripcion.contains(texto, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gastosFiltrados: StateFlow<Map<String, List<Gasto>>> = gastosFiltradosLista
        .map { gastos -> gastos.groupBy { etiquetaFecha(it.fecha) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val totalFiltrado: StateFlow<Double> = gastosFiltradosLista
        .map { gastos -> gastos.sumOf { it.monto } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val cantidadFiltrada: StateFlow<Int> = gastosFiltradosLista
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun filtrarPorCategoria(nombre: String) {
        _filtroActivo.value = FiltroHistorial.Categoria(nombre)
    }

    fun filtrarPorPeriodo(filtro: FiltroHistorial) {
        _filtroActivo.value = filtro
    }

    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch { gastoRepository.eliminar(gasto) }
    }

    private fun inicioDeHace(campo: Int, cantidad: Int, diaUnoDelMes: Boolean = false): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (diaUnoDelMes) set(Calendar.DAY_OF_MONTH, 1)
            if (cantidad != 0) add(campo, cantidad)
        }.timeInMillis
}

private val FORMATO_FECHA_CORTA = SimpleDateFormat("EEE d MMM", Locale("es", "AR"))

private fun etiquetaFecha(fechaMillis: Long): String {
    val hoy = Calendar.getInstance()
    val ayer = (hoy.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val fecha = Calendar.getInstance().apply { timeInMillis = fechaMillis }

    return when {
        mismoDia(fecha, hoy) -> "Hoy"
        mismoDia(fecha, ayer) -> "Ayer"
        else -> FORMATO_FECHA_CORTA.format(Date(fechaMillis))
    }
}

private fun mismoDia(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
