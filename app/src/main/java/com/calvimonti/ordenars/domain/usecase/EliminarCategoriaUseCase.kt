package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PresupuestoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private const val CATEGORIA_PROTEGIDA = "Otros"

/**
 * No deja borrar "Otros" (es el fallback que usa [CategorizarGastoUseCase]
 * cuando ninguna palabra clave matchea) ni una categoría con gastos ya
 * cargados — evita dejar gastos apuntando a una categoría que ya no existe.
 * Los presupuestos de la categoría sí se borran en cascada: son solo
 * configuración, no tienen sentido sin la categoría.
 */
class EliminarCategoriaUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
    private val gastoRepository: GastoRepository,
    private val presupuestoRepository: PresupuestoRepository
) {
    sealed interface Resultado {
        object Ok : Resultado
        object EsProtegida : Resultado
        data class TieneGastos(val cantidad: Int) : Resultado
    }

    suspend operator fun invoke(nombre: String): Resultado {
        if (nombre == CATEGORIA_PROTEGIDA) return Resultado.EsProtegida

        val gastos = gastoRepository.obtenerPorCategoria(nombre).first()
        if (gastos.isNotEmpty()) return Resultado.TieneGastos(gastos.size)

        presupuestoRepository.eliminarPorCategoria(nombre)
        categoriaRepository.eliminar(nombre)
        return Resultado.Ok
    }
}
