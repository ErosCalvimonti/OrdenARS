package com.calvimonti.ordenars.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.PrimarioClaro
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario

/** Rutas que se van a conectar al NavGraph (Prompt 6). */
object RutasOrdenARS {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val CATEGORIAS = "categorias"
    const val HISTORIAL = "historial"
    const val AGREGAR = "agregar"
    const val PRESUPUESTO = "presupuesto"
    const val CONFIGURACION = "configuracion"
    const val ESTADISTICAS = "estadisticas"

    /**
     * Patrón real de la ruta "agregar" en el NavGraph — con descripción y
     * categoría opcionales para precargar el formulario cuando se entra
     * desde la notificación de "compra detectada sin monto" (ver
     * [com.calvimonti.ordenars.service.NotificadorCompraDetectada]).
     * Navegar a [AGREGAR] a secas sigue funcionando: los argumentos son
     * opcionales y quedan en null.
     */
    const val AGREGAR_RUTA = "$AGREGAR?descripcion={descripcion}&categoria={categoria}"

    /**
     * Detalle de un gasto (se llega tocando una fila del Historial). El id
     * del gasto va en la ruta; [detalleGasto] arma la ruta concreta.
     */
    const val DETALLE_GASTO = "detalle_gasto"
    const val DETALLE_GASTO_RUTA = "$DETALLE_GASTO/{gastoId}"
    fun detalleGasto(gastoId: Long): String = "$DETALLE_GASTO/$gastoId"
}

@Composable
fun OrdenARSBottomBar(
    rutaActual: String,
    onNavegar: (String) -> Unit
) {
    NavigationBar(containerColor = FondoCard) {
        ItemBottomNav(
            icono = Icons.Filled.Home,
            seleccionado = rutaActual == RutasOrdenARS.DASHBOARD,
            onClick = { onNavegar(RutasOrdenARS.DASHBOARD) }
        )
        ItemBotonAgregar { onNavegar(RutasOrdenARS.AGREGAR) }
        ItemBottomNav(
            icono = Icons.AutoMirrored.Filled.List,
            seleccionado = rutaActual == RutasOrdenARS.HISTORIAL,
            onClick = { onNavegar(RutasOrdenARS.HISTORIAL) }
        )
    }
}

@Composable
private fun RowScope.ItemBottomNav(icono: ImageVector, seleccionado: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = seleccionado,
        onClick = onClick,
        icon = { Icon(icono, contentDescription = null) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Primario,
            unselectedIconColor = TextoSecundario,
            indicatorColor = FondoCard
        )
    )
}

@Composable
private fun RowScope.ItemBotonAgregar(onClick: () -> Unit) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .offset(y = (-12).dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Primario, PrimarioClaro))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar gasto", tint = TextoPrimario)
            }
        },
        colors = NavigationBarItemDefaults.colors(indicatorColor = FondoCard)
    )
}
