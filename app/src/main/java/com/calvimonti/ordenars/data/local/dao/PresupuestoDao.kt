package com.calvimonti.ordenars.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calvimonti.ordenars.data.local.entity.PresupuestoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresupuestoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(presupuesto: PresupuestoEntity)

    @Query("SELECT * FROM presupuestos WHERE mes = :mes AND anio = :anio")
    fun obtenerPorMes(mes: Int, anio: Int): Flow<List<PresupuestoEntity>>

    @Query("DELETE FROM presupuestos WHERE categoria = :categoria AND mes = :mes AND anio = :anio")
    suspend fun eliminar(categoria: String, mes: Int, anio: Int)

    // Se usa al borrar una categoría entera: sus presupuestos de cualquier
    // mes quedan sin sentido sin la categoría, se van con ella.
    @Query("DELETE FROM presupuestos WHERE categoria = :categoria")
    suspend fun eliminarPorCategoria(categoria: String)
}
