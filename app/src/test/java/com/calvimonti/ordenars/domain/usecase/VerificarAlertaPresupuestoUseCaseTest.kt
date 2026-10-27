package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PreferenciasRepository
import com.calvimonti.ordenars.data.repository.PresupuestoRepository
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.OrigenGasto
import com.calvimonti.ordenars.domain.model.Presupuesto
import com.calvimonti.ordenars.fakes.FakeDataStore
import com.calvimonti.ordenars.fakes.FakeGastoDao
import com.calvimonti.ordenars.fakes.FakePresupuestoDao
import com.calvimonti.ordenars.service.NotificadorAlertasPresupuesto
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.util.Calendar
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Presupuesto de Comida de $1.000 en el mes actual. Cada test carga gastos,
 * llama al use case como lo hace la app (después de registrar o borrar) y
 * verifica qué notificaciones push se dispararon.
 */
class VerificarAlertaPresupuestoUseCaseTest {

    private val gastoRepository = GastoRepository(FakeGastoDao())
    private val presupuestoRepository = PresupuestoRepository(FakePresupuestoDao())
    private val preferencias = PreferenciasRepository(FakeDataStore())
    private val notificador = mockk<NotificadorAlertasPresupuesto>(relaxed = true)

    private val verificar = VerificarAlertaPresupuestoUseCase(
        presupuestoRepository, gastoRepository, preferencias, notificador
    )

    private val hoy = Calendar.getInstance()
    private val mes = hoy.get(Calendar.MONTH) + 1
    private val anio = hoy.get(Calendar.YEAR)

    @Before
    fun presupuesto() = runTest {
        presupuestoRepository.guardar(Presupuesto("Comida", 1000.0, mes, anio))
    }

    private suspend fun gastar(monto: Double, categoria: String = "Comida", fecha: Long = System.currentTimeMillis()): Gasto {
        val gasto = Gasto(monto = monto, descripcion = "test", categoria = categoria, fecha = fecha, origen = OrigenGasto.MANUAL)
        return gasto.copy(id = gastoRepository.insertar(gasto))
    }

    @Test
    fun `por debajo del 80 por ciento no avisa`() = runTest {
        gastar(799.0)
        verificar("Comida")
        verify(exactly = 0) { notificador.notificar(any(), any(), any(), any()) }
    }

    @Test
    fun `justo en el 80 por ciento avisa cerca del limite`() = runTest {
        gastar(800.0)
        verificar("Comida")
        verify(exactly = 1) { notificador.notificar("Comida", 80, 1000.0, 800.0) }
    }

    @Test
    fun `no repite la alerta del 80 en el mismo mes`() = runTest {
        gastar(800.0); verificar("Comida")
        gastar(100.0); verificar("Comida")
        verify(exactly = 1) { notificador.notificar("Comida", 80, any(), any()) }
    }

    @Test
    fun `al llegar al 100 por ciento avisa que te pasaste`() = runTest {
        gastar(800.0); verificar("Comida")
        gastar(200.0); verificar("Comida")
        verifyOrder {
            notificador.notificar("Comida", 80, 1000.0, 800.0)
            notificador.notificar("Comida", 100, 1000.0, 1000.0)
        }
    }

    @Test
    fun `si salta directo al 100, al bajar a la franja 80-99 no avisa cerca del limite`() = runTest {
        val grande = gastar(1200.0); verificar("Comida")
        gastar(900.0)
        gastoRepository.eliminar(grande); verificar("Comida")

        verify(exactly = 1) { notificador.notificar("Comida", 100, any(), any()) }
        verify(exactly = 0) { notificador.notificar("Comida", 80, any(), any()) }
    }

    @Test
    fun `bajar del 80 rearma la alerta y vuelve a avisar al cruzar de nuevo`() = runTest {
        val primero = gastar(850.0); verificar("Comida")
        gastoRepository.eliminar(primero); verificar("Comida")
        gastar(820.0); verificar("Comida")
        verify(exactly = 2) { notificador.notificar("Comida", 80, any(), any()) }
    }

    @Test
    fun `volver a la franja 80-99 rearma la alerta del 100`() = runTest {
        gastar(900.0); verificar("Comida")
        val extra = gastar(150.0); verificar("Comida")
        gastoRepository.eliminar(extra); verificar("Comida")
        gastar(150.0); verificar("Comida")
        verify(exactly = 2) { notificador.notificar("Comida", 100, any(), any()) }
    }

    @Test
    fun `solo suma los gastos de esa categoria`() = runTest {
        gastar(700.0, categoria = "Transporte")
        gastar(200.0)
        verificar("Comida")
        verify(exactly = 0) { notificador.notificar(any(), any(), any(), any()) }
    }

    @Test
    fun `no cuenta gastos del mes anterior`() = runTest {
        val inicioDeMes = Calendar.getInstance().apply {
            set(anio, mes - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        gastar(900.0, fecha = inicioDeMes - 1)
        gastar(100.0)
        verificar("Comida")
        verify(exactly = 0) { notificador.notificar(any(), any(), any(), any()) }
    }

    @Test
    fun `sin presupuesto o con presupuesto en cero no avisa`() = runTest {
        gastar(5000.0, categoria = "Salud")
        verificar("Salud")
        presupuestoRepository.guardar(Presupuesto("Salud", 0.0, mes, anio))
        verificar("Salud")
        verify(exactly = 0) { notificador.notificar(any(), any(), any(), any()) }
    }

    @Test
    fun `las alertas se guardan por categoria, mes y anio`() = runTest {
        gastar(800.0); verificar("Comida")
        assertTrue(preferencias.alertaPresupuestoYaDisparada("Comida|$mes|$anio|80"))
    }
}
