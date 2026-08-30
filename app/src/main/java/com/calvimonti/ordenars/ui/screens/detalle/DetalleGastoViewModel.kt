package com.calvimonti.ordenars.ui.screens.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.Gasto
import dagger.hilt.android.lifecycle.HiltViewModel
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // El id del gasto llega en la ruta ("detalle_gasto/{gastoId}"); Compose
    // Navigation lo deja acá vía SavedStateHandle sin tocar la Activity.
    private val gastoId: Long = savedStateHandle["gastoId"] ?: -1L

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

    fun eliminar(alTerminar: () -> Unit) {
        val actual = gasto.value ?: return
        viewModelScope.launch {
            gastoRepository.eliminar(actual)
            alTerminar()
        }
    }
}
