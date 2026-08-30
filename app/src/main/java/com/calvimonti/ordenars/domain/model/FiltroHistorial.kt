package com.calvimonti.ordenars.domain.model

sealed class FiltroHistorial {
    data object Todos : FiltroHistorial()
    data object EstaSemana : FiltroHistorial()
    data object EsteMes : FiltroHistorial()
    data object Ultimos3Meses : FiltroHistorial()
    data class Categoria(val nombre: String) : FiltroHistorial()
}
