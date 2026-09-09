package com.calvimonti.ordenars.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PresupuestoRepository
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.GastoPorCategoria
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
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    gastoRepository: GastoRepository,
    categoriaRepository: CategoriaRepository,
    presupuestoRepository: PresupuestoRepository
) : ViewModel() {

    private val calendario = Calendar.getInstance()
    private val mes = calendario.get(Calendar.MONTH) + 1
    private val anio = calendario.get(Calendar.YEAR)

    val mesActual: String = NOMBRES_MESES[calendario.get(Calendar.MONTH)]

    private val gastosDelMes = gastoRepository.obtenerPorMes(mes, anio)

    val totalMesActual: StateFlow<Double> = gastosDelMes
        .map { gastos -> gastos.sumOf { it.monto } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val gastosPorCategoria: StateFlow<List<GastoPorCategoria>> = combine(
        gastosDelMes,
        categoriaRepository.obtenerTodas(),
        presupuestoRepository.obtenerPorMes(mes, anio)
    ) { gastos, categorias, presupuestos ->
        val gastadoPorCategoria = gastos.groupBy { it.categoria }
            .mapValues { (_, gastosDeCategoria) -> gastosDeCategoria.sumOf { it.monto } }

        // Esta sección es de PRESUPUESTOS: solo entran categorías con un
        // presupuesto de verdad configurado, tengan o no gasto todavía. Un
        // gasto en una categoría sin presupuesto no pinta acá — ya se ve en
        // "Últimos movimientos" — mostrarlo con una barra en 0% es engañoso
        // porque no hay presupuesto contra el cual medir ese 0%.
        presupuestos.mapNotNull { presupuesto ->
            val categoria = categorias.find { it.nombre == presupuesto.categoria } ?: return@mapNotNull null
            GastoPorCategoria(
                categoria = categoria,
                montoGastado = gastadoPorCategoria[presupuesto.categoria] ?: 0.0,
                montoPresupuestado = presupuesto.montoMaximo
            )
        }
            .sortedByDescending { it.porcentajeUsado }
            .take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Gasto del mes por categoría, de mayor a menor, para el gráfico de torta
     * del Dashboard. A diferencia de [gastosPorCategoria] (que es la sección de
     * presupuestos), acá entran TODAS las categorías con gasto, tengan o no
     * presupuesto. El detalle completo (torta + barras) está en Estadísticas.
     */
    val resumenPorCategoria: StateFlow<List<ResumenCategoria>> = combine(
        gastosDelMes,
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

    val ultimosGastos: StateFlow<List<Gasto>> = gastoRepository.obtenerTodos()
        .map { gastos -> gastos.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Categoría → porcentaje usado de su presupuesto este mes. Solo las que
     * tienen presupuesto configurado. Lo usa la fila de "Últimos movimientos"
     * para el puntito de estado al lado del ícono.
     */
    val porcentajePresupuestoPorCategoria: StateFlow<Map<String, Float>> = combine(
        presupuestoRepository.obtenerPorMes(mes, anio),
        gastosDelMes
    ) { presupuestos, gastos ->
        val gastadoPorCategoria = gastos.groupBy { it.categoria }
            .mapValues { (_, lista) -> lista.sumOf { it.monto } }
        presupuestos
            .filter { it.montoMaximo > 0 }
            .associate { p ->
                p.categoria to ((gastadoPorCategoria[p.categoria] ?: 0.0) / p.montoMaximo).toFloat()
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
