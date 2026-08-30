package com.calvimonti.ordenars.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.calvimonti.ordenars.ui.components.formatearMonto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CANAL_ALERTAS_PRESUPUESTO = "alertas_presupuesto"

/**
 * Postea la notificación de "te acercás/te pasaste de un presupuesto".
 * Requiere el permiso POST_NOTIFICATIONS (Android 13+) — si no está
 * otorgado, no hace nada (el aviso in-app en Dashboard/Presupuesto sigue
 * funcionando igual, esto es solo el empujón extra por push).
 */
@Singleton
class NotificadorAlertasPresupuesto @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    init {
        crearCanal()
    }

    private fun crearCanal() {
        val canal = NotificationChannel(
            CANAL_ALERTAS_PRESUPUESTO,
            "Alertas de presupuesto",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos cuando te acercás o te pasás de un presupuesto"
        }
        contexto.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
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
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(contexto).notify(categoria.hashCode() + umbral, notificacion)
    }
}
