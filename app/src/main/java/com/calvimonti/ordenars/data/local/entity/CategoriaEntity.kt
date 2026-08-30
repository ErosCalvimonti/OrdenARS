package com.calvimonti.ordenars.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey val nombre: String,
    val icono: String,
    val color: String,
    val palabrasClave: String // JSON con lista de palabras clave
)
