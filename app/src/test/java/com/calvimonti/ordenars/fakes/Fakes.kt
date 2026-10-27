package com.calvimonti.ordenars.fakes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.calvimonti.ordenars.data.local.dao.CategoriaDao
import com.calvimonti.ordenars.data.local.dao.GastoDao
import com.calvimonti.ordenars.data.local.dao.PresupuestoDao
import com.calvimonti.ordenars.data.local.entity.CategoriaEntity
import com.calvimonti.ordenars.data.local.entity.GastoEntity
import com.calvimonti.ordenars.data.local.entity.PresupuestoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/*
 * Implementaciones en memoria de los DAOs de Room y del DataStore, para
 * testear repositorios y use cases en la JVM sin emulador. Respetan la
 * semántica que importa de las queries reales (PK, filtros, orden).
 */

class FakeCategoriaDao : CategoriaDao {
    val tabla = MutableStateFlow<List<CategoriaEntity>>(emptyList())

    // OnConflictStrategy.IGNORE
    override suspend fun insertarTodas(categorias: List<CategoriaEntity>) {
        categorias.filter { nueva -> tabla.value.none { it.nombre == nueva.nombre } }
            .forEach { tabla.value = tabla.value + it }
    }

    // OnConflictStrategy.ABORT (sin ORDER BY, Room devuelve en orden de inserción)
    override suspend fun insertar(categoria: CategoriaEntity) {
        check(tabla.value.none { it.nombre == categoria.nombre }) { "UNIQUE constraint failed: categorias.nombre" }
        tabla.value = tabla.value + categoria
    }

    override suspend fun actualizar(categoria: CategoriaEntity) {
        tabla.value = tabla.value.map { if (it.nombre == categoria.nombre) categoria else it }
    }

    override suspend fun eliminar(nombre: String) {
        tabla.value = tabla.value.filterNot { it.nombre == nombre }
    }

    override fun obtenerTodas(): Flow<List<CategoriaEntity>> = tabla

    override suspend fun obtenerPorNombre(nombre: String): CategoriaEntity? =
        tabla.value.find { it.nombre == nombre }
}

class FakeGastoDao : GastoDao {
    val tabla = MutableStateFlow<List<GastoEntity>>(emptyList())
    private var proximoId = 1L

    override suspend fun insertar(gasto: GastoEntity): Long {
        val id = if (gasto.id == 0L) proximoId++ else gasto.id
        tabla.value = tabla.value.filterNot { it.id == id } + gasto.copy(id = id)
        return id
    }

    override suspend fun eliminar(gasto: GastoEntity) {
        tabla.value = tabla.value.filterNot { it.id == gasto.id }
    }

    override suspend fun eliminarTodos() {
        tabla.value = emptyList()
    }

    override fun obtenerTodos(): Flow<List<GastoEntity>> =
        tabla.map { gastos -> gastos.sortedByDescending { it.fecha } }

    override fun obtenerPorId(id: Long): Flow<GastoEntity?> =
        tabla.map { gastos -> gastos.find { it.id == id } }

    override fun obtenerPorCategoria(categoria: String): Flow<List<GastoEntity>> =
        tabla.map { gastos -> gastos.filter { it.categoria == categoria } }

    override fun obtenerPorMes(fechaInicio: Long, fechaFin: Long): Flow<List<GastoEntity>> =
        tabla.map { gastos -> gastos.filter { it.fecha in fechaInicio..fechaFin } }
}

class FakePresupuestoDao : PresupuestoDao {
    val tabla = MutableStateFlow<List<PresupuestoEntity>>(emptyList())

    override suspend fun insertar(presupuesto: PresupuestoEntity) {
        tabla.value = tabla.value.filterNot {
            it.categoria == presupuesto.categoria && it.mes == presupuesto.mes && it.anio == presupuesto.anio
        } + presupuesto
    }

    override fun obtenerPorMes(mes: Int, anio: Int): Flow<List<PresupuestoEntity>> =
        tabla.map { presupuestos -> presupuestos.filter { it.mes == mes && it.anio == anio } }

    override suspend fun eliminar(categoria: String, mes: Int, anio: Int) {
        tabla.value = tabla.value.filterNot { it.categoria == categoria && it.mes == mes && it.anio == anio }
    }

    override suspend fun eliminarPorCategoria(categoria: String) {
        tabla.value = tabla.value.filterNot { it.categoria == categoria }
    }
}

class FakeDataStore : DataStore<Preferences> {
    private val estado = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = estado

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val nuevo = transform(estado.value)
        estado.value = nuevo
        return nuevo
    }
}
