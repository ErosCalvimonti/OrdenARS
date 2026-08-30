package com.calvimonti.ordenars.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gastos",
    indices = [Index("categoria"), Index("fecha")]
)
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monto: Double,
    val descripcion: String,
    val categoria: String,
    val fecha: Long, // timestamp en milisegundos
    val origen: String, // "MANUAL" o "NOTIFICACION"
    val nombreBilletera: String = "", // "Personal Pay", "Naranja X", etc.
    val textoOriginal: String = "" // texto crudo de la notificación
)
