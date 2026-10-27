package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PresupuestoRepository
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.OrigenGasto
import com.calvimonti.ordenars.domain.model.Presupuesto
import com.calvimonti.ordenars.fakes.FakeCategoriaDao
import com.calvimonti.ordenars.fakes.FakeGastoDao
import com.calvimonti.ordenars.fakes.FakePresupuestoDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EliminarCategoriaUseCaseTest {

    private val categorias = CategoriaRepository(FakeCategoriaDao())
    private val gastos = GastoRepository(FakeGastoDao())
    private val presupuestos = PresupuestoRepository(FakePresupuestoDao())
    private val eliminar = EliminarCategoriaUseCase(categorias, gastos, presupuestos)

    @Before
    fun sembrar() = runTest { categorias.sembrarPorDefectoSiVacio() }

    @Test
    fun `no deja borrar Otros porque es el fallback del categorizador`() = runTest {
        assertEquals(EliminarCategoriaUseCase.Resultado.EsProtegida, eliminar("Otros"))
        assertNotNull(categorias.obtenerPorNombre("Otros"))
    }

    @Test
    fun `no deja borrar una categoria con gastos y dice cuantos tiene`() = runTest {
        repeat(3) {
            gastos.insertar(Gasto(monto = 100.0, descripcion = "Rappi", categoria = "Comida", fecha = 1L, origen = OrigenGasto.MANUAL))
        }
        assertEquals(EliminarCategoriaUseCase.Resultado.TieneGastos(3), eliminar("Comida"))
        assertNotNull(categorias.obtenerPorNombre("Comida"))
    }

    @Test
    fun `borra una categoria sin gastos junto con sus presupuestos de todos los meses`() = runTest {
        presupuestos.guardar(Presupuesto("Salud", 5000.0, 9, 2026))
        presupuestos.guardar(Presupuesto("Salud", 6000.0, 10, 2026))
        presupuestos.guardar(Presupuesto("Comida", 9000.0, 10, 2026))

        assertEquals(EliminarCategoriaUseCase.Resultado.Ok, eliminar("Salud"))

        assertNull(categorias.obtenerPorNombre("Salud"))
        assertTrue(presupuestos.obtenerPorMes(9, 2026).first().isEmpty())
        assertEquals(listOf("Comida"), presupuestos.obtenerPorMes(10, 2026).first().map { it.categoria })
    }
}
