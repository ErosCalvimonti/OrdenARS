package com.calvimonti.ordenars.data.repository

import com.calvimonti.ordenars.data.local.dao.GastoDao
import com.calvimonti.ordenars.data.local.entity.GastoEntity
import com.calvimonti.ordenars.domain.model.Gasto
import com.calvimonti.ordenars.domain.model.OrigenGasto
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class GastoRepository @Inject constructor(
    private val gastoDao: GastoDao
) {

    suspend fun insertar(gasto: Gasto): Long =
        gastoDao.insertar(gasto.aEntidad())

    suspend fun eliminar(gasto: Gasto) =
        gastoDao.eliminar(gasto.aEntidad())

    suspend fun eliminarTodos() = gastoDao.eliminarTodos()

    fun obtenerTodos(): Flow<List<Gasto>> =
        gastoDao.obtenerTodos().map { gastos -> gastos.map { it.aDominio() } }

    fun obtenerPorId(id: Long): Flow<Gasto?> =
        gastoDao.obtenerPorId(id).map { it?.aDominio() }

    fun obtenerPorCategoria(categoria: String): Flow<List<Gasto>> =
        gastoDao.obtenerPorCategoria(categoria).map { gastos -> gastos.map { it.aDominio() } }

    fun obtenerPorMes(mes: Int, anio: Int): Flow<List<Gasto>> {
        val (fechaInicio, fechaFin) = rangoDelMes(mes, anio)
        return gastoDao.obtenerPorMes(fechaInicio, fechaFin)
            .map { gastos -> gastos.map { it.aDominio() } }
    }

    private fun rangoDelMes(mes: Int, anio: Int): Pair<Long, Long> {
        val inicio = Calendar.getInstance().apply {
            set(anio, mes - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fin = (inicio.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return inicio.timeInMillis to fin.timeInMillis
    }
}

private fun GastoEntity.aDominio(): Gasto = Gasto(
    id = id,
    monto = monto,
    descripcion = descripcion,
    categoria = categoria,
    fecha = fecha,
    origen = if (origen == OrigenGasto.MANUAL.name) OrigenGasto.MANUAL else OrigenGasto.NOTIFICACION,
    nombreBilletera = nombreBilletera,
    textoOriginal = textoOriginal
)

private fun Gasto.aEntidad(): GastoEntity = GastoEntity(
    id = id,
    monto = monto,
    descripcion = descripcion,
    categoria = categoria,
    fecha = fecha,
    origen = origen.name,
    nombreBilletera = nombreBilletera,
    textoOriginal = textoOriginal
)
