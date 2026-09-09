package com.calvimonti.ordenars.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calvimonti.ordenars.ui.theme.Advertencia
import com.calvimonti.ordenars.ui.theme.Alerta
import com.calvimonti.ordenars.ui.theme.BorderSutil
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.Secundario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrdenARSCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderSutil)
    ) {
        content()
    }
}

private val formatoPesos: NumberFormat by lazy {
    NumberFormat.getNumberInstance(Locale("es", "AR")).apply {
        maximumFractionDigits = 0
    }
}

fun formatearMonto(monto: Double): String = "$${formatoPesos.format(monto)}"

/**
 * Color "semáforo" según cuánto de un presupuesto está usado, con los mismos
 * cortes que las alertas: verde hasta el 80%, ámbar entre 80 y 100%, rojo
 * pasado el límite. Se usa para el puntito de estado en las filas de gasto
 * (Dashboard e Historial) y para la sección de presupuesto del detalle.
 */
fun colorSemaforoPresupuesto(porcentajeUsado: Float): Color = when {
    porcentajeUsado >= 1f -> Alerta
    porcentajeUsado >= 0.8f -> Advertencia
    else -> Secundario
}

/**
 * Puntito de color (verde / ámbar / rojo) que se superpone al ícono de un gasto
 * para indicar cómo va el presupuesto de su categoría este mes. [colorFondo] es
 * el color de la superficie de atrás, para el "recorte" que lo despega del ícono.
 */
@Composable
fun PuntoEstadoPresupuesto(
    porcentajeUsado: Float,
    colorFondo: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(colorFondo),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colorSemaforoPresupuesto(porcentajeUsado))
        )
    }
}

@Composable
fun MontoText(
    monto: Double,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = Primario
) {
    Text(
        text = formatearMonto(monto),
        modifier = modifier,
        style = style,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
fun CategoriaChip(
    categoria: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = categoria,
            style = MaterialTheme.typography.labelMedium,
            color = TextoPrimario
        )
    }
}

@Composable
fun GradientButton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val colores = if (habilitado) {
        listOf(Primario, Secundario)
    } else {
        listOf(Primario.copy(alpha = 0.5f), Secundario.copy(alpha = 0.5f))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(colores))
            .clickable(
                enabled = habilitado,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SeccionTitulo(
    titulo: String,
    modifier: Modifier = Modifier,
    textoAccion: String? = null,
    onAccionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = TextoPrimario
        )
        if (textoAccion != null) {
            Text(
                text = textoAccion,
                style = MaterialTheme.typography.labelMedium,
                color = Primario,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = onAccionClick != null) { onAccionClick?.invoke() }
                    .padding(4.dp)
            )
        }
    }
}

/**
 * Aviso visual cuando un presupuesto está cerca (>=80%) o superado (>100%)
 * — no renderiza nada por debajo del 80%. Se usa tanto en el Dashboard como
 * en la pantalla completa de Presupuestos, para que el "banner" se vea igual
 * en los dos lugares donde puede aparecer.
 */
@Composable
fun AvisoPresupuesto(porcentajeUsado: Float, modifier: Modifier = Modifier) {
    val (color, texto) = when {
        porcentajeUsado > 1f -> Alerta to "Presupuesto superado"
        porcentajeUsado >= 0.8f -> Advertencia to "Cerca del límite"
        else -> return
    }
    Row(
        modifier = modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = color)
    }
}
