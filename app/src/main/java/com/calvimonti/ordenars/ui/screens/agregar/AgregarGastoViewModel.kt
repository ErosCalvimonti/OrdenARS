package com.calvimonti.ordenars.ui.screens.agregar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.OrigenGasto
import com.calvimonti.ordenars.domain.usecase.RegistrarGastoUseCase
import com.calvimonti.ordenars.domain.usecase.VerificarAlertaPresupuestoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AgregarGastoViewModel @Inject constructor(
    categoriaRepository: CategoriaRepository,
    private val registrarGastoUseCase: RegistrarGastoUseCase,
    private val verificarAlertaPresupuestoUseCase: VerificarAlertaPresupuestoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Vienen precargados cuando se entra desde la notificación de "compra
    // detectada sin monto" (ver NotificadorCompraDetectada) — Compose
    // Navigation los deja disponibles acá solos, sin tener que leerlos del
    // Intent de la Activity a mano. Si se entra por el botón "+" normal,
    // ninguno de los dos viene seteado y quedan en null/"".
    private val _monto = MutableStateFlow("")
    val monto: StateFlow<String> = _monto.asStateFlow()

    private val _categoriaSeleccionada =
        MutableStateFlow(savedStateHandle.get<String>("categoria"))
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada.asStateFlow()

    private val _descripcion =
        MutableStateFlow(savedStateHandle.get<String>("descripcion").orEmpty())
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()

    private val _fecha = MutableStateFlow(System.currentTimeMillis())
    val fecha: StateFlow<Long> = _fecha.asStateFlow()

    private val _gastoGuardado = MutableStateFlow(false)
    val gastoGuardado: StateFlow<Boolean> = _gastoGuardado.asStateFlow()

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val formularioValido: StateFlow<Boolean> = combine(_monto, _categoriaSeleccionada) { monto, categoria ->
        (monto.toDoubleOrNull() ?: 0.0) > 0.0 && categoria != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun onMontoChange(nuevo: String) {
        _monto.value = nuevo.filter { it.isDigit() }.take(9)
    }

    fun onCategoriaSeleccionada(nombre: String) {
        _categoriaSeleccionada.value = nombre
    }

    fun onDescripcionChange(nuevo: String) {
        _descripcion.value = nuevo.take(100)
    }

    fun onFechaChange(millis: Long) {
        _fecha.value = millis
    }

    fun guardarGasto() {
        val montoDouble = _monto.value.toDoubleOrNull() ?: return
        val categoria = _categoriaSeleccionada.value ?: return
        if (montoDouble <= 0.0) return
        viewModelScope.launch {
            registrarGastoUseCase(
                Gasto(
                    monto = montoDouble,
                    descripcion = _descripcion.value.ifBlank { categoria },
                    categoria = categoria,
                    fecha = _fecha.value,
                    origen = OrigenGasto.MANUAL
                )
            )
            verificarAlertaPresupuestoUseCase(categoria)
            _gastoGuardado.value = true
        }
    }
}
