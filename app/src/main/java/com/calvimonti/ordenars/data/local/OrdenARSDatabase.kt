package com.calvimonti.ordenars.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.calvimonti.ordenars.data.local.dao.CategoriaDao
import com.calvimonti.ordenars.data.local.dao.GastoDao
import com.calvimonti.ordenars.data.local.dao.PresupuestoDao
import com.calvimonti.ordenars.data.local.entity.CategoriaEntity
import com.calvimonti.ordenars.data.local.entity.GastoEntity
import com.calvimonti.ordenars.data.local.entity.PresupuestoEntity

@Database(
    entities = [GastoEntity::class, CategoriaEntity::class, PresupuestoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class OrdenARSDatabase : RoomDatabase() {

    abstract fun gastoDao(): GastoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun presupuestoDao(): PresupuestoDao

    companion object {
        const val NOMBRE_BASE_DATOS = "ordenars.db"
    }
}
