package com.calvimonti.ordenars.domain.model

data class Presupuesto(
    val categoria: String,
    val montoMaximo: Double,
    val mes: Int,
    val anio: Int
)
