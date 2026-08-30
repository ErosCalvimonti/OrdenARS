package com.calvimonti.ordenars.ui.screens.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.usecase.EliminarCategoriaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CategoriasViewModel @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
    private val eliminarCategoriaUseCase: EliminarCategoriaUseCase
) : ViewModel() {

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun crearCategoria(categoria: Categoria, alTerminar: () -> Unit) {
        viewModelScope.launch {
            if (categoriaRepository.crear(categoria)) {
                alTerminar()
            } else {
                _mensaje.value = "Ya existe una categoría con ese nombre"
            }
        }
    }

    fun actualizarCategoria(categoria: Categoria, alTerminar: () -> Unit) {
        viewModelScope.launch {
            categoriaRepository.actualizar(categoria)
            alTerminar()
        }
    }

    fun eliminarCategoria(nombre: String, alTerminar: () -> Unit) {
        viewModelScope.launch {
            when (val resultado = eliminarCategoriaUseCase(nombre)) {
                EliminarCategoriaUseCase.Resultado.Ok -> alTerminar()
                EliminarCategoriaUseCase.Resultado.EsProtegida ->
                    _mensaje.value = "\"Otros\" no se puede borrar — la app la usa como categoría por defecto"
                is EliminarCategoriaUseCase.Resultado.TieneGastos ->
                    _mensaje.value = "No se puede borrar: tiene ${resultado.cantidad} gasto(s) cargados en esta categoría"
            }
        }
    }

    fun ackMensaje() {
        _mensaje.value = null
    }
}
