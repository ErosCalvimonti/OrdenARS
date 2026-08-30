package com.calvimonti.ordenars.data.repository

import com.calvimonti.ordenars.data.local.dao.PresupuestoDao
import com.calvimonti.ordenars.data.local.entity.PresupuestoEntity
import com.calvimonti.ordenars.domain.model.Presupuesto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PresupuestoRepository @Inject constructor(
    private val presupuestoDao: PresupuestoDao
) {

    suspend fun guardar(presupuesto: Presupuesto) =
        presupuestoDao.insertar(presupuesto.aEntidad())

    fun obtenerPorMes(mes: Int, anio: Int): Flow<List<Presupuesto>> =
        presupuestoDao.obtenerPorMes(mes, anio).map { presupuestos -> presupuestos.map { it.aDominio() } }

    suspend fun eliminar(categoria: String, mes: Int, anio: Int) =
        presupuestoDao.eliminar(categoria, mes, anio)

    suspend fun eliminarPorCategoria(categoria: String) =
        presupuestoDao.eliminarPorCategoria(categoria)
}

private fun PresupuestoEntity.aDominio(): Presupuesto = Presupuesto(categoria, montoMaximo, mes, anio)

private fun Presupuesto.aEntidad(): PresupuestoEntity = PresupuestoEntity(categoria, montoMaximo, mes, anio)
