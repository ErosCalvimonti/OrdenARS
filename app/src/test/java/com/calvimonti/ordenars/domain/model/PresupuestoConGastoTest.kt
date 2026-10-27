package com.calvimonti.ordenars.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PresupuestoConGastoTest {

    private val comida = Categoria("Comida", "🍔", "#FFB347", emptyList())

    @Test
    fun `porcentaje usado`() {
        assertEquals(0.5f, PresupuestoConGasto(comida, 1000.0, 500.0).porcentajeUsado, 0.0001f)
        assertEquals(1.25f, PresupuestoConGasto(comida, 1000.0, 1250.0).porcentajeUsado, 0.0001f)
    }

    @Test
    fun `presupuesto en cero no divide por cero`() {
        assertEquals(0f, PresupuestoConGasto(comida, 0.0, 500.0).porcentajeUsado, 0f)
    }
}
