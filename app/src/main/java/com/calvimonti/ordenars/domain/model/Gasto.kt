package com.calvimonti.ordenars.domain.model

data class Gasto(
    val id: Long = 0,
    val monto: Double,
    val descripcion: String,
    val categoria: String,
    val fecha: Long,
    val origen: OrigenGasto,
    val nombreBilletera: String = "",
    val textoOriginal: String = ""
)

enum class OrigenGasto {
    MANUAL,
    NOTIFICACION
}
