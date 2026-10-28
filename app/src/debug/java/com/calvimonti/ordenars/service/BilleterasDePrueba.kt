package com.calvimonti.ordenars.service

/**
 * Solo en debug: las notificaciones publicadas con
 * `adb shell cmd notification post` llegan con el paquete de la shell.
 * Tratarlas como una billetera más permite que los tests E2E simulen un
 * pago real de punta a punta. El build de release usa un mapa vacío.
 */
internal val BILLETERAS_DE_PRUEBA = mapOf(
    "com.android.shell" to "Simulador (adb)"
)
