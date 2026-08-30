package com.calvimonti.ordenars.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.calvimonti.ordenars.data.local.OrdenARSDatabase
import com.calvimonti.ordenars.data.local.dao.CategoriaDao
import com.calvimonti.ordenars.data.local.dao.GastoDao
import com.calvimonti.ordenars.data.local.dao.PresupuestoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun proveerBaseDatos(@ApplicationContext contexto: Context): OrdenARSDatabase =
        Room.databaseBuilder(contexto, OrdenARSDatabase::class.java, OrdenARSDatabase.NOMBRE_BASE_DATOS)
            .build()

    @Provides
    fun proveerGastoDao(baseDatos: OrdenARSDatabase): GastoDao = baseDatos.gastoDao()

    @Provides
    fun proveerCategoriaDao(baseDatos: OrdenARSDatabase): CategoriaDao = baseDatos.categoriaDao()

    @Provides
    fun proveerPresupuestoDao(baseDatos: OrdenARSDatabase): PresupuestoDao = baseDatos.presupuestoDao()

    @Provides
    @Singleton
    fun proveerDataStorePreferencias(@ApplicationContext contexto: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            contexto.preferencesDataStoreFile("preferencias_ordenars")
        }
}
