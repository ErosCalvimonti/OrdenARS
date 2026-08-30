package com.calvimonti.ordenars.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EsquemaOscuro = darkColorScheme(
    primary = Primario,
    onPrimary = TextoPrimario,
    secondary = Secundario,
    onSecondary = FondoPrincipal,
    background = FondoPrincipal,
    onBackground = TextoPrimario,
    surface = FondoCard,
    onSurface = TextoPrimario,
    surfaceVariant = FondoSecundario,
    onSurfaceVariant = TextoSecundario,
    error = Alerta,
    onError = TextoPrimario,
    outline = BorderSutil,
)

private val FormasOrdenARS = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/** OrdenARS siempre usa dark mode — no ofrecemos tema claro. */
@Composable
fun OrdenARSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaOscuro,
        typography = Typography,
        shapes = FormasOrdenARS,
        content = content
    )
}
