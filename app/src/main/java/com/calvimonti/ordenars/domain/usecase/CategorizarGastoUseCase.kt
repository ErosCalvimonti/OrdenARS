package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.CategoriaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private const val CATEGORIA_POR_DEFECTO = "Otros"

// Palabras clave de hasta este largo tienen que aparecer como palabra
// completa: "Dia" (el súper) como substring matchea "Radio", "Claudia",
// "Diario"... Las largas siguen siendo substring para no perder
// descriptores de tarjeta pegados ("STEAMPOWERED.COM", "RAPPI*RESTO").
private const val LARGO_MAXIMO_PALABRA_COMPLETA = 3

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
            categoria.palabrasClave.any { palabraClave -> contiene(texto, palabraClave.lowercase()) }
        }
        return categoria?.nombre ?: CATEGORIA_POR_DEFECTO
    }

    private fun contiene(texto: String, palabraClave: String): Boolean {
        if (palabraClave.isBlank()) return false
        if (palabraClave.length > LARGO_MAXIMO_PALABRA_COMPLETA) return texto.contains(palabraClave)
        return Regex("""(?<![\p{L}\d])${Regex.escape(palabraClave)}(?![\p{L}\d])""").containsMatchIn(texto)
    }
}
