package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.CategoriaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private const val CATEGORIA_POR_DEFECTO = "Otros"

/**
 * Busca, entre las categorías guardadas, la primera cuya lista de
 * palabras clave aparezca (sin importar mayúsculas) dentro de la
 * descripción del gasto. Si ninguna matchea, cae en "Otros".
 */
class CategorizarGastoUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository
) {
    suspend operator fun invoke(descripcion: String): String {
        val texto = descripcion.lowercase()
        val categorias = categoriaRepository.obtenerTodas().first()
        val categoria = categorias.firstOrNull { categoria ->
            categoria.palabrasClave.any { palabraClave -> texto.contains(palabraClave.lowercase()) }
        }
        return categoria?.nombre ?: CATEGORIA_POR_DEFECTO
    }
}
