package com.calvimonti.ordenars.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val BILLETERAS_DESHABILITADAS = stringSetPreferencesKey("billeteras_deshabilitadas")
private val ONBOARDING_COMPLETADO = booleanPreferencesKey("onboarding_completado")
private val ALERTAS_PRESUPUESTO_DISPARADAS = stringSetPreferencesKey("alertas_presupuesto_disparadas")

/**
 * Guarda preferencias simples del usuario que no ameritan una tabla de Room
 * (a diferencia de Gasto/Categoria/Presupuesto, esto no es "data" del
 * dominio, es configuración de la app). Por ahora solo qué billeteras
 * (identificadas por su package name) el usuario apagó desde Configuración.
 */
@Singleton
class PreferenciasRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    fun billeteraHabilitada(paqueteBilletera: String): Flow<Boolean> =
        dataStore.data.map { prefs -> paqueteBilletera !in (prefs[BILLETERAS_DESHABILITADAS] ?: emptySet()) }

    fun billeterasDeshabilitadas(): Flow<Set<String>> =
        dataStore.data.map { prefs -> prefs[BILLETERAS_DESHABILITADAS] ?: emptySet() }

    suspend fun setBilleteraHabilitada(paqueteBilletera: String, habilitada: Boolean) {
        dataStore.edit { prefs ->
            val actuales = prefs[BILLETERAS_DESHABILITADAS] ?: emptySet()
            prefs[BILLETERAS_DESHABILITADAS] = if (habilitada) {
                actuales - paqueteBilletera
            } else {
                actuales + paqueteBilletera
            }
        }
    }

    fun onboardingCompletado(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[ONBOARDING_COMPLETADO] ?: false }

    suspend fun setOnboardingCompletado() {
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETADO] = true }
    }

    // Clave típica: "Comida|8|2026|80" — para no repetir la misma alerta de
    // presupuesto en cada gasto nuevo dentro del mismo umbral/mes/categoría.
    suspend fun alertaPresupuestoYaDisparada(clave: String): Boolean =
        clave in (dataStore.data.map { prefs -> prefs[ALERTAS_PRESUPUESTO_DISPARADAS] ?: emptySet() }.first())

    suspend fun marcarAlertaPresupuestoDisparada(clave: String) {
        dataStore.edit { prefs ->
            val actuales = prefs[ALERTAS_PRESUPUESTO_DISPARADAS] ?: emptySet()
            prefs[ALERTAS_PRESUPUESTO_DISPARADAS] = actuales + clave
        }
    }

    /** "Rearma" una alerta: la saca del set para que pueda volver a dispararse
     * si el gasto de esa categoría cruza de nuevo el umbral. */
    suspend fun desmarcarAlertaPresupuestoDisparada(clave: String) {
        dataStore.edit { prefs ->
            val actuales = prefs[ALERTAS_PRESUPUESTO_DISPARADAS] ?: return@edit
            if (clave in actuales) prefs[ALERTAS_PRESUPUESTO_DISPARADAS] = actuales - clave
        }
    }

    /** Borra todos los marcadores de alertas (se usa al limpiar el historial:
     * sin gastos, ninguna alerta tiene sentido). */
    suspend fun limpiarAlertasPresupuestoDisparadas() {
        dataStore.edit { prefs -> prefs.remove(ALERTAS_PRESUPUESTO_DISPARADAS) }
    }
}
