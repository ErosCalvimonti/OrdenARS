package com.calvimonti.ordenars

import android.app.Application
import com.calvimonti.ordenars.data.repository.CategoriaRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class OrdenARSApplication : Application() {

    @Inject lateinit var categoriaRepository: CategoriaRepository

    private val scopeApp = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        scopeApp.launch {
            categoriaRepository.sembrarPorDefectoSiVacio()
        }
    }
}
