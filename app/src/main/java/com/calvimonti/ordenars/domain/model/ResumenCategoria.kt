package com.calvimonti.ordenars.domain.model

/**
 * Cuánto se gastó en una categoría en un período. A diferencia de
 * [GastoPorCategoria], no arrastra el presupuesto — se usa para los gráficos
 * de gastos por categoría (torta y barras), donde el presupuesto no importa.
 */
data class ResumenCategoria(
    val categoria: Categoria,
    val monto: Double
)
