package com.calvimonti.ordenars.service

data class GastoDetectado(
    val monto: Double,
    val descripcion: String
)

/**
 * Parsea el texto de una notificación de billetera para extraer un gasto.
 * [parsear] solo reconoce el patrón explícito de pago con monto ("Pagaste $X
 * en Y") — notificaciones de saldo, promociones o transferencias entrantes
 * no matchean a propósito, para no registrar gastos falsos con datos que no
 * podemos interpretar bien. [parsearCompraSinMonto] cubre el otro caso real
 * confirmado: una compra avisada sin monto en el texto — no alcanza para
 * un GastoDetectado, pero sirve para avisarle al usuario. Si en el uso real
 * las billeteras mandan otras frases, hay que sumar sus regex acá.
 */
object NotificationParser {

    private val REGEX_PAGO = Regex("""[Pp]agaste?\s+\$?\s?([\d.,]+)\s+en\s+(.+)""")

    // Personal Pay (y probablemente otras billeteras) avisan una compra sin
    // mencionar el monto en ningún lado de la notificación — solo el
    // comercio ("Compraste en PEDIDOSYA*HELADERIA VEN"). Confirmado con una
    // notificación real. No hay plata que extraer acá,
    // así que esto no devuelve un GastoDetectado — parsearCompraSinMonto()
    // se usa aparte para al menos avisarle al usuario que compró algo.
    private val REGEX_COMPRA_SIN_MONTO = Regex("""[Cc]ompraste\s+en\s+(.+)""")

    fun parsear(texto: String): GastoDetectado? {
        val match = REGEX_PAGO.find(texto) ?: return null
        val monto = parsearMonto(match.groupValues[1]) ?: return null
        val descripcion = match.groupValues[2].trim().trimEnd('.', '!', '?')
        if (descripcion.isBlank()) return null
        return GastoDetectado(monto = monto, descripcion = descripcion)
    }

    /** Devuelve el nombre del comercio si el texto es una "compra sin monto", o null. */
    fun parsearCompraSinMonto(texto: String): String? {
        val match = REGEX_COMPRA_SIN_MONTO.find(texto) ?: return null
        val descripcion = match.groupValues[1].trim().trimEnd('.', '!', '?')
        return descripcion.ifBlank { null }
    }

    // Formato argentino: el punto separa miles y la coma es el decimal
    // ("1.500,50" -> 1500.50).
    private fun parsearMonto(crudo: String): Double? {
        val normalizado = crudo.trim().replace(".", "").replace(",", ".")
        return normalizado.toDoubleOrNull()
    }
}
