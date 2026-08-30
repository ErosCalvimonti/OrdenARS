package com.calvimonti.ordenars.ui.screens.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calvimonti.ordenars.ui.components.GradientButton
import com.calvimonti.ordenars.ui.theme.Advertencia
import com.calvimonti.ordenars.ui.theme.BorderSutil
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.FondoSecundario
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.Secundario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import com.calvimonti.ordenars.ui.theme.TextoTerciario
import kotlinx.coroutines.launch

private data class SlideOnboarding(val titulo: String, val subtitulo: String)

private val SLIDES = listOf(
    SlideOnboarding(
        titulo = "Ordená tus mangos",
        subtitulo = "Controlá tus gastos sin esfuerzo"
    ),
    SlideOnboarding(
        titulo = "Detección automática",
        subtitulo = "OrdenARS lee tus notificaciones de Personal Pay, Naranja X y Mercado Pago y registra tus gastos solo"
    ),
    SlideOnboarding(
        titulo = "Tu plata en orden",
        subtitulo = "Configurá presupuestos por categoría y recibí alertas antes de pasarte"
    )
)

/**
 * Se muestra una sola vez, la primera vez que se abre la app (controlado por
 * [com.calvimonti.ordenars.data.repository.PreferenciasRepository.onboardingCompletado]).
 * Tanto "Saltar" como terminar el último slide llevan al mismo diálogo de
 * permiso — la única diferencia es si el usuario vio los 3 slides o no.
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onTerminar: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val contexto = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    val scope = rememberCoroutineScope()
    var mostrarDialogoPermiso by remember { mutableStateOf(false) }
    val esUltimoSlide = pagerState.currentPage == SLIDES.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FondoPrincipal, FondoSecundario)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (!esUltimoSlide) {
                    Text(
                        text = "Saltar",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextoSecundario,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { mostrarDialogoPermiso = true }
                            .padding(8.dp)
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pagina ->
                ContenidoSlide(pagina = pagina, slide = SLIDES[pagina])
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IndicadoresPagina(cantidad = SLIDES.size, actual = pagerState.currentPage)
                Spacer(Modifier.height(28.dp))
                GradientButton(
                    texto = if (esUltimoSlide) "Empezar" else "Siguiente",
                    onClick = {
                        if (esUltimoSlide) {
                            mostrarDialogoPermiso = true
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (esUltimoSlide) 64.dp else 52.dp)
                )
            }
        }
    }

    if (mostrarDialogoPermiso) {
        DialogoPermisoNotificaciones(
            onContinuar = {
                mostrarDialogoPermiso = false
                contexto.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                viewModel.completarOnboarding(alTerminar = onTerminar)
            },
            onAhoraNo = {
                mostrarDialogoPermiso = false
                viewModel.completarOnboarding(alTerminar = onTerminar)
            }
        )
    }
}

@Composable
private fun ContenidoSlide(pagina: Int, slide: SlideOnboarding) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            when (pagina) {
                0 -> AnimacionMonedas()
                1 -> AnimacionNotificaciones()
                else -> AnimacionGrafico()
            }
        }
        Spacer(Modifier.height(48.dp))
        Text(
            text = slide.titulo,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
            color = TextoPrimario,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = slide.subtitulo,
            style = MaterialTheme.typography.bodyLarge,
            color = TextoSecundario,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IndicadoresPagina(cantidad: Int, actual: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(cantidad) { indice ->
            val activo = indice == actual
            val ancho by animateDpAsState(targetValue = if (activo) 24.dp else 8.dp, label = "ancho_indicador")
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(ancho)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (activo) Primario else TextoTerciario)
            )
        }
    }
}

// Monedas cayendo en loop, cada una con su propia fase (delay) y duración
// para que no caigan todas sincronizadas.
@Composable
private fun AnimacionMonedas(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "monedas")
    val monedas = remember {
        listOf(
            Triple(0.12f, 2200, 0),
            Triple(0.5f, 2600, 500),
            Triple(0.85f, 2000, 1000),
            Triple(0.32f, 2400, 1500)
        )
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        monedas.forEach { (xFraccion, duracionMs, delayMs) ->
            val progreso by transicion.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duracionMs, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(delayMs)
                ),
                label = "moneda_y"
            )
            val alfa = when {
                progreso < 0.15f -> progreso / 0.15f
                progreso > 0.85f -> (1f - progreso) / 0.15f
                else -> 1f
            }
            Box(
                modifier = Modifier
                    .offset(x = maxWidth * xFraccion, y = maxHeight * progreso - 28.dp)
                    .size(28.dp)
                    .alpha(alfa)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Primario, Secundario))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario
                )
            }
        }
    }
}

// Teléfono con burbujas de notificación que salen y se desvanecen en loop.
@Composable
private fun AnimacionNotificaciones(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "notificaciones")
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 150.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(FondoCard)
                .border(1.dp, BorderSutil, RoundedCornerShape(20.dp))
        )
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = null,
            tint = Primario,
            modifier = Modifier.size(28.dp)
        )

        val burbujas = remember { listOf(0 to (-32).dp, 700 to 0.dp, 1400 to 32.dp) }
        burbujas.forEach { (delayMs, offsetYInicial) ->
            val progreso by transicion.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(delayMs)
                ),
                label = "burbuja"
            )
            Box(
                modifier = Modifier
                    .offset(x = 30.dp + (70.dp * progreso), y = offsetYInicial)
                    .size(16.dp)
                    .alpha(1f - progreso)
                    .clip(CircleShape)
                    .background(Secundario)
            )
        }
    }
}

// Barras de gráfico que crecen desde abajo al entrar al slide.
@Composable
private fun AnimacionGrafico(modifier: Modifier = Modifier) {
    val alturas = remember { listOf(0.4f, 0.75f, 0.55f, 0.9f) }
    val colores = remember { listOf(Primario, Secundario, Advertencia, Primario) }
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        alturas.forEachIndexed { indice, fraccion ->
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight(fraction = (fraccion * progreso.value).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(colores[indice])
            )
        }
    }
}

@Composable
private fun DialogoPermisoNotificaciones(onContinuar: () -> Unit, onAhoraNo: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAhoraNo,
        title = { Text("Activá la detección automática") },
        text = {
            Text(
                "OrdenARS necesita acceso a tus notificaciones para leer los pagos de tus billeteras y registrar los gastos solo. No lee mensajes personales ni se conecta a internet — todo queda en tu teléfono."
            )
        },
        confirmButton = {
            TextButton(onClick = onContinuar) { Text("Continuar", color = Primario) }
        },
        dismissButton = {
            TextButton(onClick = onAhoraNo) { Text("Ahora no") }
        }
    )
}
