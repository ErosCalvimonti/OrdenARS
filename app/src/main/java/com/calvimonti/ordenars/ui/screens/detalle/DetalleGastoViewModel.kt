package com.calvimonti.ordenars.ui.screens.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PresupuestoRepository
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.GastoPorCategoria
import com.calvimonti.ordenars.domain.usecase.VerificarAlertaPresupuestoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DetalleGastoViewModel @Inject constructor(
    private val gastoRepository: GastoRepository,
    categoriaRepository: CategoriaRepository,
    presupuestoRepository: PresupuestoRepository,
    private val verificarAlertaPresupuestoUseCase: VerificarAlertaPresupuestoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // El id del gasto llega en la ruta ("detalle_gasto/{gastoId}"); Compose
    // Navigation lo deja acá vía SavedStateHandle sin tocar la Activity.
    private val gastoId: Long = savedStateHandle["gastoId"] ?: -1L

    private val calendario = Calendar.getInstance()
    private val mesActual = calendario.get(Calendar.MONTH) + 1
    private val anioActual = calendario.get(Calendar.YEAR)

    val gasto: StateFlow<Gasto?> = gastoRepository.obtenerPorId(gastoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // La categoría del gasto (para el ícono y el color) — se resuelve por
    // nombre contra la lista de categorías, igual que en el resto de la app.
    val categoria: StateFlow<Categoria?> = combine(
        gasto,
        categoriaRepository.obtenerTodas()
    ) { gastoActual, categorias ->
        categorias.find { it.nombre == gastoActual?.categoria }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Presupuesto de la categoría de este gasto para el mes en curso, con lo
     * gastado en total ese mes. `null` si la categoría no tiene presupuesto.
     * Sirve para mostrar en el detalle "a qué presupuesto pertenece" y cómo va.
     */
    val presupuestoDeCategoria: StateFlow<GastoPorCategoria?> = combine(
        gasto,
        categoria,
        presupuestoRepository.obtenerPorMes(mesActual, anioActual),
        gastoRepository.obtenerPorMes(mesActual, anioActual)
    ) { gastoActual, categoriaActual, presupuestos, gastosDelMes ->
        val nombreCategoria = gastoActual?.categoria ?: return@combine null
        val categoriaResuelta = categoriaActual ?: return@combine null
        val presupuesto = presupuestos
            .find { it.categoria == nombreCategoria && it.montoMaximo > 0 }
            ?: return@combine null
        val gastado = gastosDelMes
            .filter { it.categoria == nombreCategoria }
            .sumOf { it.monto }
        GastoPorCategoria(
            categoria = categoriaResuelta,
            montoGastado = gastado,
            montoPresupuestado = presupuesto.montoMaximo
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun eliminar(alTerminar: () -> Unit) {
        val actual = gasto.value ?: return
        viewModelScope.launch {
            gastoRepository.eliminar(actual)
            // Rearma la alerta de esa categoría si el borrado la dejó bajo el umbral.
            verificarAlertaPresupuestoUseCase(actual.categoria)
            alTerminar()
        }
    }
}
