package com.calvimonti.ordenars.domain.model

data class GastoPorCategoria(
    val categoria: Categoria,
    val montoGastado: Double,
    val montoPresupuestado: Double?
) {
    /** Puede superar 1f si el gasto pasó el presupuesto. */
    val porcentajeUsado: Float
        get() = if (montoPresupuestado != null && montoPresupuestado > 0) {
            (montoGastado / montoPresupuestado).toFloat()
        } else {
            0f
        }
}
