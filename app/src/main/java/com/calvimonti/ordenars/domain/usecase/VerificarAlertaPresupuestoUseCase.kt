package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PreferenciasRepository
import com.calvimonti.ordenars.data.repository.PresupuestoRepository
import com.calvimonti.ordenars.service.NotificadorAlertasPresupuesto
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Se llama después de registrar O borrar un gasto. Recalcula cuánto se lleva
 * gastado este mes en esa categoría y, según en qué franja quedó:
 *
 * - **>= 100%**: dispara la alerta de "te pasaste" (si no se disparó ya este mes).
 * - **80–99%**: dispara la de "cerca del límite" (si no se disparó ya), y
 *   *rearma* la de 100% — así, si volvés a cruzar el 100% después de un borrado,
 *   vuelve a avisar.
 * - **< 80%**: rearma las dos. Volviste abajo de todo, la próxima vez que
 *   cruces cualquiera de los dos umbrales te vuelve a avisar.
 *
 * "Disparar" acá es solo la notificación push. El aviso visual en
 * Dashboard/Presupuesto no pasa por acá — se recalcula solo, en vivo.
 */
class VerificarAlertaPresupuestoUseCase @Inject constructor(
    private val presupuestoRepository: PresupuestoRepository,
    private val gastoRepository: GastoRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val notificador: NotificadorAlertasPresupuesto
) {
    suspend operator fun invoke(categoria: String) {
        val calendario = Calendar.getInstance()
        val mes = calendario.get(Calendar.MONTH) + 1
        val anio = calendario.get(Calendar.YEAR)

        val presupuesto = presupuestoRepository.obtenerPorMes(mes, anio).first()
            .find { it.categoria == categoria && it.montoMaximo > 0 } ?: return

        val gastado = gastoRepository.obtenerPorMes(mes, anio).first()
            .filter { it.categoria == categoria }
            .sumOf { it.monto }

        val porcentaje = gastado / presupuesto.montoMaximo
        val clave80 = "$categoria|$mes|$anio|80"
        val clave100 = "$categoria|$mes|$anio|100"

        when {
            porcentaje >= 1.0 -> {
                // Ya se cruzó el 80% aunque no se haya notificado en su momento
                // (p. ej. si el primer gasto saltó directo por encima del 100%):
                // lo marcamos en silencio para que no salte "cerca del límite"
                // cuando el monto baje de vuelta a la franja 80–99%.
                preferenciasRepository.marcarAlertaPresupuestoDisparada(clave80)
                dispararSiHaceFalta(clave100) {
                    notificador.notificar(categoria, 100, presupuesto.montoMaximo, gastado)
                }
            }
            porcentaje >= 0.8 -> {
                preferenciasRepository.desmarcarAlertaPresupuestoDisparada(clave100)
                dispararSiHaceFalta(clave80) {
                    notificador.notificar(categoria, 80, presupuesto.montoMaximo, gastado)
                }
            }
            else -> {
                preferenciasRepository.desmarcarAlertaPresupuestoDisparada(clave80)
                preferenciasRepository.desmarcarAlertaPresupuestoDisparada(clave100)
            }
        }
    }

    private suspend fun dispararSiHaceFalta(clave: String, notificar: () -> Unit) {
        if (preferenciasRepository.alertaPresupuestoYaDisparada(clave)) return
        preferenciasRepository.marcarAlertaPresupuestoDisparada(clave)
        notificar()
    }
}
