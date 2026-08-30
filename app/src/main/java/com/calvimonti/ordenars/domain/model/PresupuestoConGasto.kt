package com.calvimonti.ordenars.domain.model

data class PresupuestoConGasto(
    val categoria: Categoria,
    val montoMaximo: Double,
    val montoGastado: Double
) {
    val porcentajeUsado: Float
        get() = if (montoMaximo > 0) (montoGastado / montoMaximo).toFloat() else 0f
}
