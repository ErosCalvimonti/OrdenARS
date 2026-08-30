package com.calvimonti.ordenars.data.local.entity

import androidx.room.Entity

@Entity(tableName = "presupuestos", primaryKeys = ["categoria", "mes", "anio"])
data class PresupuestoEntity(
    val categoria: String,
    val montoMaximo: Double,
    val mes: Int,
    val anio: Int
)
