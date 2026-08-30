# OrdenARS — Contexto completo del proyecto

## ¿Qué es OrdenARS?

App Android nativa en Kotlin que detecta automáticamente gastos desde notificaciones de billeteras virtuales argentinas (Personal Pay, Naranja X, Mercado Pago), los categoriza y muestra dashboards con gráficos para ordenar las finanzas personales.

---

## Datos del proyecto

- **Nombre:** OrdenARS
- **Package name:** com.calvimonti.ordenars
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose
- **Minimum SDK:** API 26 (Android 8.0)
- **Build system:** Gradle con Kotlin DSL (build.gradle.kts)
- **Arquitectura:** MVVM + Clean Architecture (capas: UI, Domain, Data)

---

## Arquitectura — Estructura de carpetas

```
com.calvimonti.ordenars/
├── data/
│   ├── local/
│   │   ├── OrdenARSDatabase.kt       ← Room Database
│   │   ├── dao/
│   │   │   ├── GastoDao.kt
│   │   │   ├── CategoriaDao.kt
│   │   │   └── PresupuestoDao.kt
│   │   └── entity/
│   │       ├── GastoEntity.kt
│   │       ├── CategoriaEntity.kt
│   │       └── PresupuestoEntity.kt
│   └── repository/
│       ├── GastoRepository.kt
│       ├── CategoriaRepository.kt
│       └── PresupuestoRepository.kt
├── domain/
│   ├── model/
│   │   ├── Gasto.kt
│   │   ├── Categoria.kt
│   │   └── Presupuesto.kt
│   └── usecase/
│       ├── RegistrarGastoUseCase.kt
│       ├── ObtenerGastosUseCase.kt
│       ├── CategorizarGastoUseCase.kt
│       └── VerificarPresupuestoUseCase.kt
├── ui/
│   ├── screens/
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt
│   │   │   └── DashboardViewModel.kt
│   │   ├── historial/
│   │   │   ├── HistorialScreen.kt
│   │   │   └── HistorialViewModel.kt
│   │   ├── agregar/
│   │   │   ├── AgregarGastoScreen.kt
│   │   │   └── AgregarGastoViewModel.kt
│   │   ├── presupuesto/
│   │   │   ├── PresupuestoScreen.kt
│   │   │   └── PresupuestoViewModel.kt
│   │   └── configuracion/
│   │       └── ConfiguracionScreen.kt
│   ├── components/
│   │   ├── GastoCard.kt
│   │   ├── GraficoTorta.kt
│   │   └── PresupuestoBar.kt
│   ├── navigation/
│   │   └── NavGraph.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── service/
│   ├── OrdenARSNotificationListener.kt  ← EL CORAZON DEL PROYECTO
│   └── NotificationParser.kt            ← Parsea el texto con Regex
└── di/
    └── AppModule.kt                     ← Hilt dependency injection
```

---

## Base de datos — Entidades Room

### GastoEntity
```kotlin
@Entity(tableName = "gastos")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monto: Double,
    val descripcion: String,
    val categoria: String,
    val fecha: Long,                    // timestamp en milisegundos
    val origen: String,                 // "MANUAL" o "NOTIFICACION"
    val nombreBilletera: String = "",   // "Personal Pay", "Naranja X", etc.
    val textoOriginal: String = ""      // texto crudo de la notificación
)
```

### CategoriaEntity
```kotlin
@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey val nombre: String,
    val icono: String,
    val color: String,
    val palabrasClave: String           // JSON con lista de palabras clave
)
```

### PresupuestoEntity
```kotlin
@Entity(tableName = "presupuestos")
data class PresupuestoEntity(
    @PrimaryKey val categoria: String,
    val montoMaximo: Double,
    val mes: Int,
    val anio: Int
)
```

---

## Categorías predefinidas con palabras clave

| Categoría     | Palabras clave                                              |
|---------------|-------------------------------------------------------------|
| Comida        | McDonald's, Rappi, PedidosYa, supermercado, Carrefour, Dia |
| Transporte    | SUBE, Uber, Cabify, nafta, peaje, YPF, Shell               |
| Entretenimiento | Netflix, Spotify, Steam, cinema, cine                     |
| Servicios     | Edesur, Edenor, Metrogas, Telecom, Personal, Movistar      |
| Salud         | farmacia, médico, clínica, OSDE, Swiss Medical             |
| Otros         | (todo lo que no matchea ninguna categoría)                  |

---

## NotificationListenerService — Lógica central

### Flujo completo:
1. Billetera manda notificación → Android la intercepta
2. `OrdenARSNotificationListener` recibe el evento `onNotificationPosted`
3. Filtra solo notificaciones de apps de billeteras conocidas
4. Pasa el texto al `NotificationParser`
5. El parser usa Regex para extraer monto y comercio
6. Llama al `RegistrarGastoUseCase`
7. El use case categoriza y guarda en Room
8. La UI se actualiza automáticamente via Flow

### Package names de billeteras a escuchar:
```kotlin
val BILLETERAS_CONOCIDAS = setOf(
    "com.personal.mp",              // Personal Pay
    "ar.com.naranja.x",             // Naranja X
    "com.mercadopago.wallet",       // Mercado Pago
    "ar.com.santander.bank",        // Santander
    "com.bbva.nativemobile",        // BBVA
    "ar.com.galicia.android"        // Galicia
)
```

### Regex para parsear montos:
```kotlin
// Detecta patrones como: "$1.500", "$ 1500,50", "1.500,00"
val REGEX_MONTO = Regex("""\$\s?[\d.,]+""")

// Detecta patrones como: "Pagaste $500 en McDonalds"
val REGEX_PAGO = Regex("""[Pp]agaste?\s+\$?\s?[\d.,]+\s+en\s+(.+)""")

// Detecta patrones como: "Transferencia de $1000"
val REGEX_TRANSFERENCIA = Regex("""[Tt]ransferencia\s+de\s+\$?\s?[\d.,]+""")
```

### Permiso especial en AndroidManifest.xml:
```xml
<service
    android:name=".service.OrdenARSNotificationListener"
    android:label="OrdenARS"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

---

## Dependencias — build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ViewModel + Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navegación
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt (inyección de dependencias)
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Gráficos
    implementation("co.yml:ycharts:2.1.0")
}
```

---

## Pantallas de la app

### 1. Dashboard (pantalla principal)
- Total gastado este mes
- Gráfico de torta por categoría
- Lista de últimos 5 gastos
- Indicador de presupuesto (cuánto queda)

### 2. Historial
- Lista completa de gastos
- Filtro por categoría
- Filtro por rango de fechas
- Chip diferenciador: MANUAL vs AUTOMÁTICO

### 3. Agregar gasto (formulario manual)
- Campo monto
- Selector de categoría
- Campo descripción
- Selector de fecha

### 4. Presupuestos
- Lista de categorías con monto máximo
- Barra de progreso por categoría
- Alerta visual cuando supera el 80%

### 5. Configuración
- Toggle para activar/desactivar el listener
- Botón para ir a los permisos del sistema
- Lista de billeteras activas

---

## Decisiones de diseño tomadas

- **Sin backend ni login** — todo local con Room, funciona offline
- **Sin autenticación** — app personal, un solo usuario
- **Categorización por palabras clave** — lista editable, no ML
- **Notificaciones locales** — cuando se supera el presupuesto
- **Sin sincronización en la nube** — fuera del alcance del proyecto
- **Idioma:** español argentino en toda la UI

---

## Estado actual del proyecto

- Proyecto Android creado en Android Studio
- Arquitectura MVVM definida
- Estructura de carpetas lista para implementar
- **Próximo paso:** implementar GastoEntity + GastoDao + Room Database

---

## Convenciones de desarrollo

Al trabajar en este proyecto:
1. Siempre usá Kotlin idiomático (data classes, extension functions, coroutines)
2. Seguí la arquitectura MVVM estrictamente — la UI nunca toca la DB directamente
3. Usá `StateFlow` para el estado de la UI, no `LiveData`
4. Todos los strings de la UI en español argentino
5. Cada feature en su propia carpeta dentro de `ui/screens/`
6. El `NotificationParser` debe ser fácilmente extensible para nuevas billeteras
7. Manejá siempre los casos donde el Regex no matchea (retornar null, no crashear)
