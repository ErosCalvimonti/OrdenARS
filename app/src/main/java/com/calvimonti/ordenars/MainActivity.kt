package com.calvimonti.ordenars

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.calvimonti.ordenars.data.repository.PreferenciasRepository
import com.calvimonti.ordenars.service.EXTRA_RUTA_INICIAL
import com.calvimonti.ordenars.ui.navigation.OrdenARSNavGraph
import com.calvimonti.ordenars.ui.navigation.RutasOrdenARS
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.OrdenARSTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferenciasRepository: PreferenciasRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrdenARSTheme {
                // El onboarding solo se muestra la primera vez — hay que
                // leer el flag de DataStore antes de decidir el punto de
                // partida del NavHost, así que arrancamos en null (loading)
                // para no mostrar un flash de la pantalla equivocada.
                var pantallaInicial by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    val yaCompletado = preferenciasRepository.onboardingCompletado().first()
                    pantallaInicial = if (yaCompletado) RutasOrdenARS.DASHBOARD else RutasOrdenARS.ONBOARDING
                }

                val destinoInicial = pantallaInicial
                if (destinoInicial == null) {
                    Box(modifier = Modifier.fillMaxSize().background(FondoPrincipal))
                    return@OrdenARSTheme
                }

                // Si la Activity se abrió desde la notificación de "compra
                // detectada sin monto" (NotificadorCompraDetectada), el
                // Intent trae la ruta de Agregar Gasto ya armada con
                // descripción/categoría precargadas. Ojo: esto NO reemplaza
                // el destino inicial (Dashboard/Onboarding) — se empuja
                // ENCIMA (ver OrdenARSNavGraph) para que quede algo debajo
                // en el historial. Si "agregar" fuera directamente el
                // startDestination, no habría a dónde volver al guardar
                // (popBackStack() no tiene nada que sacar) y la pantalla de
                // confirmación se queda trabada — pasó de verdad, ver
                // devlog 2026-08-28.
                val rutaDesdeNotificacion = intent.getStringExtra(EXTRA_RUTA_INICIAL)

                OrdenARSNavGraph(
                    navController = rememberNavController(),
                    startDestination = destinoInicial,
                    rutaExtra = rutaDesdeNotificacion
                )
            }
        }
    }
}
