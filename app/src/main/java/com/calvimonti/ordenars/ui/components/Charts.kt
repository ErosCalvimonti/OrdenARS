package com.calvimonti.ordenars.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calvimonti.ordenars.ui.theme.FondoSecundario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import kotlin.math.roundToInt

/**
 * Un dato para los gráficos: una porción con su etiqueta, su valor absoluto,
 * el color con el que se dibuja y (opcional) un emoji para la leyenda / las
 * barras. Se arma en el ViewModel/pantalla para no acoplar los gráficos a los
 * modelos de dominio.
 */
data class RebanadaGrafico(
    val etiqueta: String,
    val valor: Double,
    val color: Color,
    val icono: String = ""
)

/**
 * Gráfico de torta (en realidad una dona) de [datos] según su
 * [RebanadaGrafico.valor], con una leyenda al costado. Las porciones entran
 * animadas la primera vez que se muestran.
 *
 * Si se pasa [maxRebanadas], las porciones que sobran se juntan en una sola
 * llamada "Otras" — sirve para lugares con poco espacio, como el Dashboard.
 */
@Composable
fun GraficoTorta(
    datos: List<RebanadaGrafico>,
    modifier: Modifier = Modifier,
    tamanoDona: Dp = 150.dp,
    maxRebanadas: Int? = null,
    colorOtras: Color = TextoSecundario
) {
    val datosMostrados = remember(datos, maxRebanadas) {
        if (maxRebanadas == null || datos.size <= maxRebanadas) {
            datos
        } else {
            val visibles = datos.take(maxRebanadas - 1)
            val resto = datos.drop(maxRebanadas - 1).sumOf { it.valor }
            visibles + RebanadaGrafico("Otras", resto, colorOtras)
        }
    }
    val total = datosMostrados.sumOf { it.valor }
    if (datosMostrados.isEmpty() || total <= 0.0) return

    val progreso = remember { Animatable(0f) }
    LaunchedEffect(datosMostrados) {
        progreso.snapTo(0f)
        progreso.animateTo(1f, tween(700))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Canvas(modifier = Modifier.size(tamanoDona)) {
            val grosor = size.minDimension * 0.18f
            val diametro = size.minDimension - grosor
            val esquina = Offset((size.width - diametro) / 2f, (size.height - diametro) / 2f)
            val tamanoArco = Size(diametro, diametro)
            val trazo = Stroke(width = grosor, cap = StrokeCap.Butt)

            var anguloInicio = -90f
            datosMostrados.forEach { rebanada ->
                val barrido = (rebanada.valor / total).toFloat() * 360f
                val separacion = if (barrido > 6f) 2f else 0f
                drawArc(
                    color = rebanada.color,
                    startAngle = anguloInicio,
                    sweepAngle = (barrido * progreso.value - separacion).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = esquina,
                    size = tamanoArco,
                    style = trazo
                )
                anguloInicio += barrido
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            datosMostrados.forEach { rebanada ->
                val porcentaje = (rebanada.valor / total * 100).roundToInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(rebanada.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = rebanada.etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoPrimario,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$porcentaje%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                }
            }
        }
    }
}

/**
 * Gráfico de barras horizontales de [datos] según su [RebanadaGrafico.valor].
 * La barra más larga es la del valor máximo; el resto se escala contra ese.
 * Las barras entran animadas la primera vez.
 */
@Composable
fun GraficoBarras(
    datos: List<RebanadaGrafico>,
    modifier: Modifier = Modifier
) {
    val maximo = datos.maxOfOrNull { it.valor } ?: 0.0
    if (datos.isEmpty() || maximo <= 0.0) return

    val progreso = remember { Animatable(0f) }
    LaunchedEffect(datos) {
        progreso.snapTo(0f)
        progreso.animateTo(1f, tween(700))
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        datos.forEach { rebanada ->
            val fraccion = (rebanada.valor / maximo).toFloat().coerceIn(0f, 1f)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${rebanada.icono} ${rebanada.etiqueta}".trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoPrimario
                    )
                    Text(
                        text = formatearMonto(rebanada.valor),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoPrimario
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(FondoSecundario)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraccion * progreso.value)
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(rebanada.color)
                    )
                }
            }
        }
    }
}
