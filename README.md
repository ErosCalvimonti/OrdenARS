# OrdenARS

App Android nativa (Kotlin + Jetpack Compose) que detecta automáticamente los gastos desde las notificaciones de billeteras virtuales argentinas (Personal Pay, Naranja X, Mercado Pago, entre otras), los categoriza y muestra dashboards para ordenar las finanzas personales. Todo funciona 100% offline, sin backend ni login.

Para el detalle completo de arquitectura y decisiones de diseño, ver [`CONTEXTO_ORDENARS.md`](./CONTEXTO_ORDENARS.md).

## Stack

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM + Clean Architecture (`ui` / `domain` / `data`)
- **Base de datos:** Room
- **Inyección de dependencias:** Hilt
- **Async:** Coroutines + Flow
- **Mínimo SDK:** API 26 (Android 8.0)

## Requisitos

- Android Studio (Ladybug o superior)
- JDK 17
- Android SDK con platform 35 y build-tools 34 instalados

## Cómo correrlo

1. Abrí la carpeta del proyecto en Android Studio (`File → Open...`)
2. Esperá el sync de Gradle
3. Conectá un dispositivo físico o levantá un emulador (`Tools → Device Manager`)
4. Run ▶️

También se puede compilar e instalar por terminal:

```bash
./gradlew installDebug
```

> **Nota:** el `NotificationListenerService` que detecta gastos automáticamente solo se puede probar con notificaciones *reales* de billeteras en un dispositivo físico con esas apps instaladas — en el emulador no hay forma de recibirlas. Para probar el pipeline (parser + categorización + guardado) sin eso, se puede otorgar el permiso por `adb` y postear una notificación de prueba.

## Estado actual

Aplicación funcional, en etapa de pruebas. Ver `REQUERIMIENTOS.md` para la especificación completa y el estado de cumplimiento de cada requerimiento.

Implementado:
- Arquitectura completa (`data` / `domain` / `ui` / `service` / `di`), Room, Hilt, DataStore.
- Sistema de diseño (`Color`, `Type` con fuente Inter, `Theme` dark-only, `Components`, `Charts`).
- Pantallas: **Onboarding**, **Dashboard** (total del mes + gráfico de torta por categoría), **Agregar Gasto**, **Historial** (búsqueda, filtros, swipe para borrar), **Detalle de gasto**, **Presupuestos** (agregar / editar / eliminar), **Estadísticas** (torta + barras), **Configuración**, **Editor de categorías**.
- Navegación con `NavHost`; bottom nav de 3 ítems (Dashboard — Agregar — Historial).
- Detección automática: `NotificationParser` + `CategorizarGastoUseCase` + `OrdenARSNotificationListener`, con flujo de "compra detectada sin monto". Probada end-to-end en dispositivo físico.
- Alertas de presupuesto al 80 % / 100 % (aviso in-app + notificación push).
- Exportación del historial a CSV.
- Gráficos de torta y barras dibujados con `Canvas`, sin librerías.

Pendiente:
- Un caso real confirmado del patrón "Pagaste $X en Y" (con el monto en el texto de la notificación).
- Suite de tests automatizados (el testing hasta ahora es funcional manual).
- Registro de ingresos (hoy la app solo trackea egresos).

## Estructura de carpetas

```
com.calvimonti.ordenars/
├── data/
│   ├── local/          # Room: entities, DAOs, database
│   └── repository/
├── domain/
│   ├── model/
│   └── usecase/
├── ui/
│   ├── screens/
│   ├── components/
│   ├── navigation/
│   └── theme/
├── service/             # NotificationListenerService + parser
└── di/                  # Módulos de Hilt
```
