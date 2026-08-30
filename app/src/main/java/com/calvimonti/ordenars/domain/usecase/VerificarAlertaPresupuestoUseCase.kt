package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.data.repository.PreferenciasRepository
import com.calvimonti.ordenars.data.repository.PresupuestoRepository
import com.calvimonti.ordenars.service.NotificadorAlertasPresupuesto
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Se llama después de registrar un gasto. Si esa categoría tiene presupuesto
 * este mes y el gasto acumulado cruzó el 80% o el 100%, dispara la alerta
 * (push + queda marcada para no repetirla en el próximo gasto del mismo
 * umbral/mes/categoría). El aviso visual en Dashboard/Presupuesto no pasa
 * por acá — ese se recalcula solo, en vivo, a partir del gasto guardado.
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
        val umbral = when {
            porcentaje >= 1.0 -> 100
            porcentaje >= 0.8 -> 80
            else -> return
        }

        val clave = "$categoria|$mes|$anio|$umbral"
        if (preferenciasRepository.alertaPresupuestoYaDisparada(clave)) return

        preferenciasRepository.marcarAlertaPresupuestoDisparada(clave)
        notificador.notificar(categoria, umbral, presupuesto.montoMaximo, gastado)
    }
}
