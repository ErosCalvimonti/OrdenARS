package com.calvimonti.ordenars.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.calvimonti.ordenars.ui.screens.agregar.AgregarGastoScreen
import com.calvimonti.ordenars.ui.screens.categorias.CategoriasScreen
import com.calvimonti.ordenars.ui.screens.configuracion.ConfiguracionScreen
import com.calvimonti.ordenars.ui.screens.dashboard.DashboardScreen
import com.calvimonti.ordenars.ui.screens.detalle.DetalleGastoScreen
import com.calvimonti.ordenars.ui.screens.estadisticas.EstadisticasScreen
import com.calvimonti.ordenars.ui.screens.historial.HistorialScreen
import com.calvimonti.ordenars.ui.screens.onboarding.OnboardingScreen
import com.calvimonti.ordenars.ui.screens.presupuesto.PresupuestoScreen

private const val DURACION_TRANSICION_MS = 200

/**
 * NavGraph del Prompt 6: fade suave (200ms) entre pantallas normales.
 * "agregar" es la excepción — entra con slide desde abajo como un modal de
 * verdad, así que la pantalla de atrás se queda quieta (sin fade) en vez de
 * desvanecerse, tanto al abrirlo como al cerrarlo.
 */
@Composable
fun OrdenARSNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    // Ruta con la que "empujar" el NavHost apenas arranca, ENCIMA de
    // startDestination — no la reemplaza. La usa la notificación de "compra
    // detectada sin monto" para abrir Agregar Gasto precargado con algo
    // debajo en el historial (si "agregar" fuera directamente el
    // startDestination, popBackStack() al guardar no tendría a dónde volver
    // y la pantalla se queda trabada — pasó de verdad, ver devlog 2026-08-28).
    rutaExtra: String? = null
) {
    LaunchedEffect(rutaExtra) {
        if (rutaExtra != null) navController.navigate(rutaExtra)
    }

    val irA: (String) -> Unit = { ruta ->
        when (ruta) {
            RutasOrdenARS.AGREGAR, RutasOrdenARS.HISTORIAL,
            RutasOrdenARS.PRESUPUESTO, RutasOrdenARS.CONFIGURACION,
            RutasOrdenARS.CATEGORIAS, RutasOrdenARS.ESTADISTICAS ->
                navController.navigate(ruta)
            RutasOrdenARS.DASHBOARD -> navController.navigate(ruta) {
                popUpTo(RutasOrdenARS.DASHBOARD)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(tween(DURACION_TRANSICION_MS)) },
        exitTransition = {
            if (targetState.destination.route == RutasOrdenARS.AGREGAR_RUTA) {
                ExitTransition.None
            } else {
                fadeOut(tween(DURACION_TRANSICION_MS))
            }
        },
        popEnterTransition = {
            if (initialState.destination.route == RutasOrdenARS.AGREGAR_RUTA) {
                EnterTransition.None
            } else {
                fadeIn(tween(DURACION_TRANSICION_MS))
            }
        },
        popExitTransition = { fadeOut(tween(DURACION_TRANSICION_MS)) }
    ) {
        composable(RutasOrdenARS.ONBOARDING) {
            OnboardingScreen(
                onTerminar = {
                    navController.navigate(RutasOrdenARS.DASHBOARD) {
                        popUpTo(RutasOrdenARS.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(RutasOrdenARS.DASHBOARD) {
            DashboardScreen(
                onNavegar = irA,
                onVerTodoMovimientos = { irA(RutasOrdenARS.HISTORIAL) },
                onVerTodoPresupuesto = { irA(RutasOrdenARS.PRESUPUESTO) },
                onVerEstadisticas = { irA(RutasOrdenARS.ESTADISTICAS) },
                onClickConfiguracion = { irA(RutasOrdenARS.CONFIGURACION) }
            )
        }
        composable(RutasOrdenARS.HISTORIAL) {
            HistorialScreen(
                onNavegar = irA,
                onGastoClick = { gasto ->
                    navController.navigate(RutasOrdenARS.detalleGasto(gasto.id))
                }
            )
        }
        composable(RutasOrdenARS.PRESUPUESTO) {
            PresupuestoScreen(onNavegar = irA)
        }
        composable(RutasOrdenARS.CONFIGURACION) {
            ConfiguracionScreen(onNavegar = irA)
        }
        composable(RutasOrdenARS.CATEGORIAS) {
            CategoriasScreen(onVolver = { navController.popBackStack() })
        }
        composable(
            route = RutasOrdenARS.DETALLE_GASTO_RUTA,
            arguments = listOf(navArgument("gastoId") { type = NavType.LongType })
        ) {
            DetalleGastoScreen(onVolver = { navController.popBackStack() })
        }
        composable(RutasOrdenARS.ESTADISTICAS) {
            EstadisticasScreen(onVolver = { navController.popBackStack() })
        }
        composable(
            route = RutasOrdenARS.AGREGAR_RUTA,
            arguments = listOf(
                navArgument("descripcion") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("categoria") { type = NavType.StringType; nullable = true; defaultValue = null }
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn() },
            exitTransition = { slideOutVertically(targetOffsetY = { it }) + fadeOut() }
        ) {
            // descripción/categoría llegan vía SavedStateHandle al ViewModel
            // (ver AgregarGastoViewModel) cuando se entra desde la
            // notificación de "compra detectada sin monto" — navegar acá
            // a secas (botón "+") deja ambos argumentos en null.
            AgregarGastoScreen(onVolver = { navController.popBackStack() })
        }
    }
}
