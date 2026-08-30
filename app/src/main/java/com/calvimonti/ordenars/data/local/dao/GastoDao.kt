package com.calvimonti.ordenars.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calvimonti.ordenars.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: GastoEntity): Long

    @Delete
    suspend fun eliminar(gasto: GastoEntity)

    @Query("DELETE FROM gastos")
    suspend fun eliminarTodos()

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE id = :id")
    fun obtenerPorId(id: Long): Flow<GastoEntity?>

    @Query("SELECT * FROM gastos WHERE categoria = :categoria ORDER BY fecha DESC")
    fun obtenerPorCategoria(categoria: String): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha DESC")
    fun obtenerPorMes(fechaInicio: Long, fechaFin: Long): Flow<List<GastoEntity>>
}
