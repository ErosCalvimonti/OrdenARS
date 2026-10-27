package com.calvimonti.ordenars.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationParserTest {

    // --- parsear: pago con monto -------------------------------------------

    @Test
    fun `pago simple extrae monto y comercio`() {
        assertEquals(GastoDetectado(1500.0, "Rappi"), NotificationParser.parsear("Pagaste \$1.500 en Rappi"))
    }

    @Test
    fun `monto en formato argentino con miles y decimales`() {
        assertEquals(1500.50, NotificationParser.parsear("Pagaste \$1.500,50 en Carrefour")!!.monto, 0.001)
        assertEquals(12_345_678.0, NotificationParser.parsear("Pagaste \$12.345.678 en Garbarino")!!.monto, 0.001)
        assertEquals(12.5, NotificationParser.parsear("Pagaste \$12,5 en Kiosco")!!.monto, 0.001)
    }

    @Test
    fun `acepta espacio despues del signo, sin signo, minuscula y la variante 'Pagast'`() {
        assertEquals(GastoDetectado(800.0, "SUBE"), NotificationParser.parsear("Pagaste $ 800 en SUBE"))
        assertEquals(GastoDetectado(800.0, "SUBE"), NotificationParser.parsear("pagaste 800 en SUBE"))
        assertEquals(GastoDetectado(800.0, "SUBE"), NotificationParser.parsear("Pagast \$800 en SUBE"))
    }

    @Test
    fun `saca la puntuacion final del comercio`() {
        assertEquals("McDonald's", NotificationParser.parsear("Pagaste \$4.200 en McDonald's.")!!.descripcion)
        assertEquals("Netflix", NotificationParser.parsear("¡Pagaste \$7.999 en Netflix!")!!.descripcion)
    }

    @Test
    fun `encuentra el pago aunque haya texto antes`() {
        val gasto = NotificationParser.parsear("Mercado Pago: Pagaste \$2.000 en Uber")
        assertEquals(GastoDetectado(2000.0, "Uber"), gasto)
    }

    @Test
    fun `descriptor de tarjeta con asterisco queda entero`() {
        assertEquals("PEDIDOSYA*HELADERIA VEN", NotificationParser.parsear("Pagaste \$3.100 en PEDIDOSYA*HELADERIA VEN")!!.descripcion)
    }

    @Test
    fun `notificaciones que no son un pago no registran nada`() {
        listOf(
            "Te transfirieron \$5.000",
            "Tu saldo es \$12.000",
            "Recibiste \$3.000 de Juan",
            "¡20% de descuento en Rappi!",
            "Compraste en PEDIDOSYA*HELADERIA VEN",
            "",
        ).forEach { assertNull(it, NotificationParser.parsear(it)) }
    }

    @Test
    fun `monto que no es numero no registra nada`() {
        assertNull(NotificationParser.parsear("Pagaste \$.,. en Rappi"))
    }

    @Test
    fun `sin comercio no registra nada`() {
        assertNull(NotificationParser.parsear("Pagaste \$1.500 en "))
        assertNull(NotificationParser.parsear("Pagaste \$1.500 en ..."))
    }

    // --- parsearNotificacion: título y cuerpo por separado ------------------

    @Test
    fun `pago en el titulo no se mezcla con el boilerplate del cuerpo`() {
        // Antes se parseaba "titulo + texto" y el comercio quedaba
        // "Kiosco Con tu tarjeta Personal Pay" (y "Personal" -> Servicios).
        val gasto = NotificationParser.parsearNotificacion(
            titulo = "Pagaste \$1.500 en Kiosco",
            texto = "Con tu tarjeta Personal Pay"
        )
        assertEquals(GastoDetectado(1500.0, "Kiosco"), gasto)
    }

    @Test
    fun `pago en el cuerpo con el nombre de la billetera como titulo`() {
        val gasto = NotificationParser.parsearNotificacion(titulo = "Mercado Pago", texto = "Pagaste \$2.300 en Rappi")
        assertEquals(GastoDetectado(2300.0, "Rappi"), gasto)
    }

    @Test
    fun `pago partido entre titulo y cuerpo se arma con los dos`() {
        val gasto = NotificationParser.parsearNotificacion(titulo = "Pagaste \$990", texto = "en Spotify")
        assertEquals(GastoDetectado(990.0, "Spotify"), gasto)
    }

    @Test
    fun `notificacion sin pago devuelve null`() {
        assertNull(NotificationParser.parsearNotificacion("Mercado Pago", "Tu saldo es \$12.000"))
    }

    // --- parsearCompraSinMonto -------------------------------------------------

    @Test
    fun `compra sin monto devuelve el comercio`() {
        assertEquals("PEDIDOSYA*HELADERIA VEN", NotificationParser.parsearCompraSinMonto("Compraste en PEDIDOSYA*HELADERIA VEN"))
        assertEquals("Farmacity", NotificationParser.parsearCompraSinMonto("compraste en Farmacity."))
    }

    @Test
    fun `compra sin monto ignora lo que no es una compra`() {
        assertNull(NotificationParser.parsearCompraSinMonto("Pagaste \$1.500 en Rappi"))
        assertNull(NotificationParser.parsearCompraSinMonto("Compraste en "))
        assertNull(NotificationParser.parsearCompraSinMonto("Compraste en !"))
    }
}
