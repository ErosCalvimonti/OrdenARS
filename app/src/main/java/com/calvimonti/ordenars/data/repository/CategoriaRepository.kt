package com.calvimonti.ordenars.data.repository

import com.calvimonti.ordenars.data.local.dao.CategoriaDao
import com.calvimonti.ordenars.data.local.entity.CategoriaEntity
import com.calvimonti.ordenars.domain.model.Categoria
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

@Singleton
class CategoriaRepository @Inject constructor(
    private val categoriaDao: CategoriaDao
) {

    fun obtenerTodas(): Flow<List<Categoria>> =
        categoriaDao.obtenerTodas().map { categorias -> categorias.map { it.aDominio() } }

    suspend fun obtenerPorNombre(nombre: String): Categoria? =
        categoriaDao.obtenerPorNombre(nombre)?.aDominio()

    suspend fun sembrarPorDefectoSiVacio() {
        if (categoriaDao.obtenerTodas().first().isEmpty()) {
            categoriaDao.insertarTodas(CATEGORIAS_POR_DEFECTO)
        }
    }

    /** false si ya existe una categoría con ese nombre (es la PK). */
    suspend fun crear(categoria: Categoria): Boolean {
        if (categoriaDao.obtenerPorNombre(categoria.nombre) != null) return false
        categoriaDao.insertar(categoria.aEntidad())
        return true
    }

    suspend fun actualizar(categoria: Categoria) {
        categoriaDao.actualizar(categoria.aEntidad())
    }

    suspend fun eliminar(nombre: String) {
        categoriaDao.eliminar(nombre)
    }
}

private fun palabrasAJson(palabras: List<String>): String = JSONArray(palabras).toString()

private fun Categoria.aEntidad(): CategoriaEntity = CategoriaEntity(
    nombre = nombre,
    icono = icono,
    color = color,
    palabrasClave = palabrasAJson(palabrasClave)
)

private fun CategoriaEntity.aDominio(): Categoria {
    val array = if (palabrasClave.isBlank()) JSONArray() else JSONArray(palabrasClave)
    return Categoria(
        nombre = nombre,
        icono = icono,
        color = color,
        palabrasClave = List(array.length()) { array.getString(it) }
    )
}

private val CATEGORIAS_POR_DEFECTO = listOf(
    CategoriaEntity(
        nombre = "Comida",
        icono = "🍔",
        color = "#FFB347",
        palabrasClave = palabrasAJson(listOf("McDonald's", "Rappi", "PedidosYa", "supermercado", "Carrefour", "Dia"))
    ),
    CategoriaEntity(
        nombre = "Transporte",
        icono = "🚗",
        color = "#00D4AA",
        palabrasClave = palabrasAJson(listOf("SUBE", "Uber", "Cabify", "nafta", "peaje", "YPF", "Shell"))
    ),
    CategoriaEntity(
        nombre = "Entretenimiento",
        icono = "🎮",
        color = "#6C63FF",
        palabrasClave = palabrasAJson(listOf("Netflix", "Spotify", "Steam", "cinema", "cine"))
    ),
    CategoriaEntity(
        nombre = "Servicios",
        icono = "💡",
        color = "#4FC3F7",
        palabrasClave = palabrasAJson(listOf("Edesur", "Edenor", "Metrogas", "Telecom", "Personal", "Movistar"))
    ),
    CategoriaEntity(
        nombre = "Salud",
        icono = "💊",
        color = "#FF6B6B",
        palabrasClave = palabrasAJson(listOf("farmacia", "médico", "clínica", "OSDE", "Swiss Medical"))
    ),
    CategoriaEntity(
        nombre = "Otros",
        icono = "📦",
        color = "#9E9E9E",
        palabrasClave = palabrasAJson(emptyList())
    ),
)
