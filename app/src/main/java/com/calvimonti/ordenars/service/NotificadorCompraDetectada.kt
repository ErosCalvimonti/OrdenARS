package com.calvimonti.ordenars.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.calvimonti.ordenars.MainActivity
import com.calvimonti.ordenars.ui.navigation.RutasOrdenARS
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CANAL_COMPRA_DETECTADA = "compra_detectada"

/** Nombre del extra del Intent que [MainActivity] lee para saber con qué
 * ruta arrancar el NavGraph en vez del punto de entrada normal. */
const val EXTRA_RUTA_INICIAL = "ruta_inicial"

/**
 * Postea el aviso de "detectamos una compra pero la notificación no traía
 * el monto" (ver [NotificationParser.parsearCompraSinMonto] — Personal Pay
 * es el primer caso real confirmado, devlog 2026-08-28). Tocar la
 * notificación abre Agregar Gasto con la categoría y descripción ya
 * precargadas — el usuario solo tiene que escribir el monto.
 *
 * Igual que [NotificadorAlertasPresupuesto], requiere POST_NOTIFICATIONS
 * (Android 13+); si no está otorgado, no pasa nada — la compra simplemente
 * no queda registrada hasta que el usuario la cargue a mano desde el botón
 * "+" de siempre.
 */
@Singleton
class NotificadorCompraDetectada @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    init {
        crearCanal()
    }

    private fun crearCanal() {
        val canal = NotificationChannel(
            CANAL_COMPRA_DETECTADA,
            "Compras detectadas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de compras detectadas sin monto, para cargar a mano"
        }
        contexto.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
    }

    fun notificar(descripcion: String, categoria: String) {
        val tienePermiso = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!tienePermiso) return

        // Ruta con los mismos nombres de argumento que declara AGREGAR_RUTA
        // en el NavGraph — MainActivity la pasa tal cual como startDestination
        // del NavHost, y Compose Navigation se encarga de extraer
        // descripción/categoría de la query para el ViewModel.
        val ruta = "${RutasOrdenARS.AGREGAR}?descripcion=${Uri.encode(descripcion)}&categoria=${Uri.encode(categoria)}"
        val intent = Intent(contexto, MainActivity::class.java).apply {
            putExtra(EXTRA_RUTA_INICIAL, ruta)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            contexto,
            descripcion.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(contexto, CANAL_COMPRA_DETECTADA)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Compraste en $descripcion")
            .setContentText("Tocá para cargar el monto")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(contexto).notify(descripcion.hashCode(), notificacion)
    }
}
