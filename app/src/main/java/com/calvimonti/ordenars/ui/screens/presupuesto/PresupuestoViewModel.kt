package com.calvimonti.ordenars.ui.screens.presupuesto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PresupuestoRepository
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.Presupuesto
import com.calvimonti.ordenars.domain.model.PresupuestoConGasto
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val NOMBRES_MESES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

@HiltViewModel
class PresupuestoViewModel @Inject constructor(
    private val presupuestoRepository: PresupuestoRepository,
    gastoRepository: GastoRepository,
    categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val calendario = Calendar.getInstance()
    private val mes = calendario.get(Calendar.MONTH) + 1
    private val anio = calendario.get(Calendar.YEAR)

    val tituloPeriodo: String = "${NOMBRES_MESES[calendario.get(Calendar.MONTH)]} $anio"

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val presupuestosPorCategoria: StateFlow<List<PresupuestoConGasto>> = combine(
        presupuestoRepository.obtenerPorMes(mes, anio),
        gastoRepository.obtenerPorMes(mes, anio),
        categoriaRepository.obtenerTodas()
    ) { presupuestos, gastos, categorias ->
        presupuestos.mapNotNull { presupuesto ->
            val categoria = categorias.find { it.nombre == presupuesto.categoria } ?: return@mapNotNull null
            PresupuestoConGasto(
                categoria = categoria,
                montoMaximo = presupuesto.montoMaximo,
                montoGastado = gastos.filter { it.categoria == presupuesto.categoria }.sumOf { it.monto }
            )
        }.sortedByDescending { it.porcentajeUsado }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalPresupuestado: StateFlow<Double> = presupuestosPorCategoria
        .map { lista -> lista.sumOf { it.montoMaximo } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalGastado: StateFlow<Double> = presupuestosPorCategoria
        .map { lista -> lista.sumOf { it.montoGastado } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun agregarPresupuesto(categoria: String, monto: Double, mesElegido: Int = mes, anioElegido: Int = anio) {
        viewModelScope.launch {
            presupuestoRepository.guardar(Presupuesto(categoria, monto, mesElegido, anioElegido))
        }
    }

    fun editarPresupuesto(categoria: String, monto: Double) {
        agregarPresupuesto(categoria, monto)
    }

    fun eliminarPresupuesto(categoria: String) {
        viewModelScope.launch {
            presupuestoRepository.eliminar(categoria, mes, anio)
        }
    }
}
