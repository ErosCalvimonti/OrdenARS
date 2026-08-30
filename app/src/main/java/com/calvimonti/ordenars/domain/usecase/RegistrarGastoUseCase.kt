package com.calvimonti.ordenars.domain.usecase

import com.calvimonti.ordenars.data.repository.GastoRepository
import com.calvimonti.ordenars.domain.model.Gasto
import javax.inject.Inject

class RegistrarGastoUseCase @Inject constructor(
    private val gastoRepository: GastoRepository
) {
    suspend operator fun invoke(gasto: Gasto): Long = gastoRepository.insertar(gasto)
}
