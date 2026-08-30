package com.calvimonti.ordenars.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.calvimonti.ordenars.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodas(categorias: List<CategoriaEntity>)

    // ABORT (default de @Insert) para poder distinguir "ya existe" del resto
    // de errores — el repositorio chequea el nombre antes, esto es la red
    // de seguridad ante una carrera entre dos inserts simultáneos.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(categoria: CategoriaEntity)

    @Update
    suspend fun actualizar(categoria: CategoriaEntity)

    @Query("DELETE FROM categorias WHERE nombre = :nombre")
    suspend fun eliminar(nombre: String)

    @Query("SELECT * FROM categorias")
    fun obtenerTodas(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE nombre = :nombre")
    suspend fun obtenerPorNombre(nombre: String): CategoriaEntity?
}
