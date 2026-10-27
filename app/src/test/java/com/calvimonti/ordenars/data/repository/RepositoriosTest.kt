package com.calvimonti.ordenars.data.repository

import com.calvimonti.ordenars.data.local.dao.GastoDao
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.OrigenGasto
import com.calvimonti.ordenars.fakes.FakeCategoriaDao
import com.calvimonti.ordenars.fakes.FakeDataStore
import com.calvimonti.ordenars.fakes.FakeGastoDao
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.Calendar
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoriaRepositoryTest {

    private val dao = FakeCategoriaDao()
    private val repositorio = CategoriaRepository(dao)

    @Test
    fun `siembra las 6 categorias por defecto una sola vez`() = runTest {
        repositorio.sembrarPorDefectoSiVacio()
        repositorio.sembrarPorDefectoSiVacio()
        assertEquals(
            listOf("Comida", "Transporte", "Entretenimiento", "Servicios", "Salud", "Otros"),
            repositorio.obtenerTodas().first().map { it.nombre }
        )
    }

    @Test
    fun `no siembra encima de las categorias del usuario`() = runTest {
        repositorio.crear(Categoria("Mascotas", "🐶", "#000000", emptyList()))
        repositorio.sembrarPorDefectoSiVacio()
        assertEquals(listOf("Mascotas"), repositorio.obtenerTodas().first().map { it.nombre })
    }

    @Test
    fun `crear rechaza un nombre repetido`() = runTest {
        assertTrue(repositorio.crear(Categoria("Mascotas", "🐶", "#000000", emptyList())))
        assertFalse(repositorio.crear(Categoria("Mascotas", "🐱", "#FFFFFF", emptyList())))
        assertEquals("🐶", repositorio.obtenerPorNombre("Mascotas")!!.icono)
    }

    @Test
    fun `las palabras clave sobreviven el viaje a JSON con comillas, acentos y emojis`() = runTest {
        val palabras = listOf("McDonald's", "médico", "\"comillas\"", "café ☕", "a,b")
        repositorio.crear(Categoria("Rara", "❓", "#000000", palabras))
        assertEquals(palabras, repositorio.obtenerPorNombre("Rara")!!.palabrasClave)
    }

    @Test
    fun `actualizar cambia palabras clave sin duplicar la categoria`() = runTest {
        repositorio.crear(Categoria("Mascotas", "🐶", "#000000", listOf("Puppis")))
        repositorio.actualizar(Categoria("Mascotas", "🐶", "#000000", listOf("Puppis", "veterinaria")))
        val todas = repositorio.obtenerTodas().first()
        assertEquals(1, todas.size)
        assertEquals(listOf("Puppis", "veterinaria"), todas.single().palabrasClave)
    }
}

class GastoRepositoryTest {

    @Test
    fun `obtenerPorMes pide desde el primer milisegundo hasta el ultimo del mes`() {
        val dao = mockk<GastoDao>()
        val inicio = slot<Long>()
        val fin = slot<Long>()
        every { dao.obtenerPorMes(capture(inicio), capture(fin)) } returns emptyFlow()

        GastoRepository(dao).obtenerPorMes(mes = 2, anio = 2024) // bisiesto

        val desde = Calendar.getInstance().apply { timeInMillis = inicio.captured }
        val hasta = Calendar.getInstance().apply { timeInMillis = fin.captured }
        assertEquals(listOf(2024, Calendar.FEBRUARY, 1, 0, 0, 0, 0), campos(desde))
        assertEquals(listOf(2024, Calendar.FEBRUARY, 29, 23, 59, 59, 999), campos(hasta))
    }

    @Test
    fun `diciembre termina el 31 sin pasarse al anio siguiente`() {
        val dao = mockk<GastoDao>()
        val fin = slot<Long>()
        every { dao.obtenerPorMes(any(), capture(fin)) } returns emptyFlow()

        GastoRepository(dao).obtenerPorMes(mes = 12, anio = 2025)

        val hasta = Calendar.getInstance().apply { timeInMillis = fin.captured }
        assertEquals(listOf(2025, Calendar.DECEMBER, 31, 23, 59, 59, 999), campos(hasta))
    }

    @Test
    fun `mapea origen y datos de la billetera ida y vuelta`() = runTest {
        val repositorio = GastoRepository(FakeGastoDao())
        val gasto = Gasto(
            monto = 1500.5, descripcion = "Rappi", categoria = "Comida", fecha = 123L,
            origen = OrigenGasto.NOTIFICACION, nombreBilletera = "Mercado Pago",
            textoOriginal = "Pagaste \$1.500,50 en Rappi"
        )
        val id = repositorio.insertar(gasto)
        assertEquals(gasto.copy(id = id), repositorio.obtenerPorId(id).first())
    }

    private fun campos(c: Calendar) = listOf(
        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND)
    )
}

class PreferenciasRepositoryTest {

    private val preferencias = PreferenciasRepository(FakeDataStore())

    @Test
    fun `las billeteras arrancan habilitadas y se pueden apagar y prender`() = runTest {
        assertTrue(preferencias.billeteraHabilitada("com.mercadopago.wallet").first())

        preferencias.setBilleteraHabilitada("com.mercadopago.wallet", false)
        assertFalse(preferencias.billeteraHabilitada("com.mercadopago.wallet").first())
        assertTrue(preferencias.billeteraHabilitada("com.brubank").first())

        preferencias.setBilleteraHabilitada("com.mercadopago.wallet", true)
        assertTrue(preferencias.billeteraHabilitada("com.mercadopago.wallet").first())
    }

    @Test
    fun `marcar, desmarcar y limpiar alertas de presupuesto`() = runTest {
        preferencias.marcarAlertaPresupuestoDisparada("Comida|10|2026|80")
        preferencias.marcarAlertaPresupuestoDisparada("Comida|10|2026|100")
        assertTrue(preferencias.alertaPresupuestoYaDisparada("Comida|10|2026|80"))

        preferencias.desmarcarAlertaPresupuestoDisparada("Comida|10|2026|80")
        assertFalse(preferencias.alertaPresupuestoYaDisparada("Comida|10|2026|80"))
        assertTrue(preferencias.alertaPresupuestoYaDisparada("Comida|10|2026|100"))

        preferencias.limpiarAlertasPresupuestoDisparadas()
        assertFalse(preferencias.alertaPresupuestoYaDisparada("Comida|10|2026|100"))
    }

    @Test
    fun `desmarcar sin nada marcado no rompe`() = runTest {
        preferencias.desmarcarAlertaPresupuestoDisparada("Comida|10|2026|80")
        assertFalse(preferencias.alertaPresupuestoYaDisparada("Comida|10|2026|80"))
    }

    @Test
    fun `onboarding arranca sin completar`() = runTest {
        assertFalse(preferencias.onboardingCompletado().first())
        preferencias.setOnboardingCompletado()
        assertTrue(preferencias.onboardingCompletado().first())
    }
}
