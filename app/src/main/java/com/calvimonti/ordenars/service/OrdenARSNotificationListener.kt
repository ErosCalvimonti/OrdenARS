package com.calvimonti.ordenars.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.calvimonti.ordenars.data.repository.PreferenciasRepository
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.OrigenGasto
import com.calvimonti.ordenars.domain.usecase.CategorizarGastoUseCase
import com.calvimonti.ordenars.domain.usecase.RegistrarGastoUseCase
import com.calvimonti.ordenars.domain.usecase.VerificarAlertaPresupuestoUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Escucha TODAS las notificaciones que llegan al teléfono (una vez que el
 * usuario habilita "Acceso a notificaciones" para OrdenARS en los Ajustes
 * del sistema — Android no permite pedir este permiso con un diálogo común,
 * hay que ir a Ajustes). Filtra por [BILLETERAS_CONOCIDAS], intenta parsear
 * un gasto con [NotificationParser] y, si matchea, lo categoriza y guarda.
 */
@AndroidEntryPoint
class OrdenARSNotificationListener : NotificationListenerService() {

    @Inject lateinit var categorizarGastoUseCase: CategorizarGastoUseCase
    @Inject lateinit var registrarGastoUseCase: RegistrarGastoUseCase
    @Inject lateinit var verificarAlertaPresupuestoUseCase: VerificarAlertaPresupuestoUseCase
    @Inject lateinit var preferenciasRepository: PreferenciasRepository
    @Inject lateinit var notificadorCompraDetectada: NotificadorCompraDetectada

    // onNotificationPosted no es suspend, así que necesitamos un scope propio
    // para poder llamar a los use cases (que sí son suspend porque tocan Room).
    private val scopeServicio = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val nombreBilletera = BILLETERAS_CONOCIDAS[sbn.packageName]
            ?: BILLETERAS_DE_PRUEBA[sbn.packageName]
            ?: return

        val extras = sbn.notification.extras
        val titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val texto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val textoCompleto = "$titulo $texto".trim()
        if (textoCompleto.isBlank()) return

        val gastoDetectado = NotificationParser.parsearNotificacion(titulo, texto)
        if (gastoDetectado != null) {
            scopeServicio.launch {
                val habilitada = preferenciasRepository.billeteraHabilitada(sbn.packageName).first()
                if (!habilitada) return@launch

                val categoria = categorizarGastoUseCase(gastoDetectado.descripcion)
                registrarGastoUseCase(
                    Gasto(
                        monto = gastoDetectado.monto,
                        descripcion = gastoDetectado.descripcion,
                        categoria = categoria,
                        fecha = sbn.postTime,
                        origen = OrigenGasto.NOTIFICACION,
                        nombreBilletera = nombreBilletera,
                        textoOriginal = textoCompleto
                    )
                )
                verificarAlertaPresupuestoUseCase(categoria)
            }
            return
        }

        // Sin monto en el texto (confirmado con Personal Pay): no hay nada que guardar todavía, pero avisamos con
        // una notificación para que el usuario complete el monto a mano.
        // Ojo: se parsea SOLO el título, no textoCompleto — el cuerpo suele
        // ser un boilerplate genérico ("Con tu tarjeta Personal Pay") sin
        // relación al comercio, y como el regex es ".+" greedy, si se le
        // pasa el texto concatenado se lo come entero de yapa (mismo motivo
        // por el que el pago con monto usa parsearNotificacion).
        val comercio = NotificationParser.parsearCompraSinMonto(titulo) ?: return
        scopeServicio.launch {
            val habilitada = preferenciasRepository.billeteraHabilitada(sbn.packageName).first()
            if (!habilitada) return@launch

            val categoria = categorizarGastoUseCase(comercio)
            notificadorCompraDetectada.notificar(descripcion = comercio, categoria = categoria)
        }
    }

    companion object {
        // Package names verificados contra Play Store.
        val BILLETERAS_CONOCIDAS = mapOf(
            // Billeteras y bancos con presencia fuerte en Argentina
            "ar.com.personalpay" to "Personal Pay",
            "com.tarjetanaranja.ncuenta" to "Naranja X",
            "com.mercadopago.wallet" to "Mercado Pago",
            "ar.com.santander.rio.mbanking" to "Santander",
            "com.bbva.nxt_argentina" to "BBVA",
            "com.mosync.app_Banco_Galicia" to "Galicia",
            "ar.com.bna.digital" to "Banco Nación",
            "ar.com.bancoprovincia.CuentaDNI" to "Cuenta DNI",
            "ar.com.bancar.uala" to "Ualá",
            "com.brubank" to "Brubank",
            "com.wbank.mobileapp" to "Wilobank",
            "com.playdigital.modo" to "MODO",
            // Apps de cripto / dólar digital
            "com.applemoncash" to "Lemon Cash",
            "com.belo.android" to "Belo",
            "com.buengroup.buenbit" to "Buenbit",
            "com.bitso.wallet" to "Bitso",
            "com.binance.dev" to "Binance",
            "com.dolarapp" to "DolarApp",
            "com.dukascopy.swissbank" to "Dukascopy",
            "com.azc.azcoiner" to "AZCoiner",
            // Otras formas de pago / transferencias
            "com.global66.cards" to "Global66",
            "com.transferwise.android" to "Wise",
            "com.samsung.android.spay" to "Samsung Pay",
            "com.sube.cargasube" to "SUBE"
        )
    }
}
