package com.calvimonti.ordenars.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.calvimonti.ordenars.MainActivity
import com.calvimonti.ordenars.ui.components.formatearMonto
import com.calvimonti.ordenars.ui.navigation.RutasOrdenARS
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Se cambió el id (antes "alertas_presupuesto") porque un canal ya creado no
// puede subir su importancia por código — solo el usuario, desde Ajustes. Con
// un id nuevo, Android lo crea de cero como IMPORTANCE_HIGH y la alerta asoma.
private const val CANAL_ALERTAS_PRESUPUESTO = "alertas_presupuesto_v2"
private const val CANAL_ALERTAS_PRESUPUESTO_VIEJO = "alertas_presupuesto"

/**
 * Postea la notificación de "te acercás/te pasaste de un presupuesto".
 * Requiere el permiso POST_NOTIFICATIONS (Android 13+) — si no está
 * otorgado, no hace nada (el aviso in-app en Dashboard/Presupuesto sigue
 * funcionando igual, esto es solo el empujón extra por push).
 *
 * Tocar la notificación abre la pantalla de Presupuestos.
 */
@Singleton
class NotificadorAlertasPresupuesto @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    init {
        crearCanal()
    }

    private fun crearCanal() {
        val manager = contexto.getSystemService(NotificationManager::class.java) ?: return
        manager.deleteNotificationChannel(CANAL_ALERTAS_PRESUPUESTO_VIEJO)
        val canal = NotificationChannel(
            CANAL_ALERTAS_PRESUPUESTO,
            "Alertas de presupuesto",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos cuando te acercás o te pasás de un presupuesto"
        }
        manager.createNotificationChannel(canal)
    }

    fun notificar(categoria: String, umbral: Int, montoMaximo: Double, montoGastado: Double) {
        val tienePermiso = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!tienePermiso) return

        val (titulo, texto) = if (umbral >= 100) {
            "Te pasaste del presupuesto de $categoria" to
                "Gastaste ${formatearMonto(montoGastado)} de ${formatearMonto(montoMaximo)} presupuestados este mes."
        } else {
            "Estás cerca del límite de $categoria" to
                "Ya usaste el 80% de tu presupuesto de $categoria este mes."
        }

        val notificacion = NotificationCompat.Builder(contexto, CANAL_ALERTAS_PRESUPUESTO)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setContentIntent(intentAPresupuestos())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(contexto).notify(categoria.hashCode() + umbral, notificacion)
    }

    private fun intentAPresupuestos(): PendingIntent {
        val intent = Intent(contexto, MainActivity::class.java).apply {
            putExtra(EXTRA_RUTA_INICIAL, RutasOrdenARS.PRESUPUESTO)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            contexto,
            RutasOrdenARS.PRESUPUESTO.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
