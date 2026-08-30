package com.calvimonti.ordenars.ui.screens.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.domain.model.ResumenCategoria
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val NOMBRES_MESES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    gastoRepository: GastoRepository,
    categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val calendario = Calendar.getInstance()
    private val mes = calendario.get(Calendar.MONTH) + 1
    private val anio = calendario.get(Calendar.YEAR)

    val tituloPeriodo: String = "${NOMBRES_MESES[calendario.get(Calendar.MONTH)]} $anio"

    /**
     * Gasto total del mes por categoría, de mayor a menor. Entran solo las
     * categorías que tienen al menos un gasto este mes (no las que están en 0).
     */
    val resumenPorCategoria: StateFlow<List<ResumenCategoria>> = combine(
        gastoRepository.obtenerPorMes(mes, anio),
        categoriaRepository.obtenerTodas()
    ) { gastos, categorias ->
        gastos.groupBy { it.categoria }
            .mapNotNull { (nombreCategoria, gastosDeCategoria) ->
                val categoria = categorias.find { it.nombre == nombreCategoria }
                    ?: return@mapNotNull null
                ResumenCategoria(categoria, gastosDeCategoria.sumOf { it.monto })
            }
            .sortedByDescending { it.monto }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val total: StateFlow<Double> = resumenPorCategoria
        .map { lista -> lista.sumOf { it.monto } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)
}
