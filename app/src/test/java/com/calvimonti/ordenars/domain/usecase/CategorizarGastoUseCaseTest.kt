package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.CategoriaRepository
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.fakes.FakeCategoriaDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Corre contra las categorías que la app siembra por defecto. */
class CategorizarGastoUseCaseTest {

    private val repositorio = CategoriaRepository(FakeCategoriaDao())
    private val categorizar = CategorizarGastoUseCase(repositorio)

    @Before
    fun sembrar() = runTest { repositorio.sembrarPorDefectoSiVacio() }

    @Test
    fun `cada palabra clave por defecto lleva a su categoria`() = runTest {
        mapOf(
            "Rappi" to "Comida",
            "PEDIDOSYA*HELADERIA VEN" to "Comida",
            "Carga SUBE" to "Transporte",
            "YPF Av. Libertador" to "Transporte",
            "NETFLIX.COM" to "Entretenimiento",
            "Spotify" to "Entretenimiento",
            "Edenor" to "Servicios",
            "Farmacia del Pueblo" to "Salud",
            "Swiss Medical" to "Salud",
        ).forEach { (descripcion, esperada) ->
            assertEquals(descripcion, esperada, categorizar(descripcion))
        }
    }

    @Test
    fun `no distingue mayusculas`() = runTest {
        assertEquals("Comida", categorizar("RAPPI*RESTO"))
        assertEquals("Transporte", categorizar("uber trip"))
    }

    @Test
    fun `sin coincidencias cae en Otros`() = runTest {
        assertEquals("Otros", categorizar("Ferretería Don José"))
        assertEquals("Otros", categorizar(""))
    }

    @Test
    fun `palabra clave corta solo matchea como palabra completa`() = runTest {
        // "Dia" (el súper) como substring metía todo esto en Comida.
        assertEquals("Otros", categorizar("Radio Taxi"))
        assertEquals("Otros", categorizar("Peluquería Claudia"))
        assertEquals("Otros", categorizar("Diario Clarín"))
        // Como palabra suelta sigue andando, incluso pegada a puntuación.
        assertEquals("Comida", categorizar("DIA %"))
        assertEquals("Comida", categorizar("Supermercado-Dia"))
        assertEquals("Transporte", categorizar("YPF"))
    }

    @Test
    fun `gana la primera categoria que matchea`() = runTest {
        // "Uber" es Transporte; no hay palabra de Comida en "Uber Eats".
        assertEquals("Transporte", categorizar("Uber Eats"))
        // "Rappi" (Comida) aparece antes que "Personal" (Servicios).
        assertEquals("Comida", categorizar("Rappi Personal"))
    }

    @Test
    fun `usa las categorias y palabras clave editadas por el usuario`() = runTest {
        repositorio.crear(Categoria("Mascotas", "🐶", "#000000", listOf("Puppis", "veterinaria")))
        assertEquals("Mascotas", categorizar("PUPPIS PALERMO"))
        assertEquals("Mascotas", categorizar("Veterinaria San Roque"))
    }

    @Test
    fun `una palabra clave vacia no matchea todo`() = runTest {
        repositorio.crear(Categoria("Vacía", "❓", "#000000", listOf("")))
        assertEquals("Otros", categorizar("Ferretería"))
    }
}
