# OrdenARS — Especificación de Requerimientos

**Versión:** 1.0 · **Fecha:** 2026-08-28 · **Estado del producto:** funcional, en pruebas

Documento formal de requerimientos funcionales (RF) y no funcionales (RNF) de la
aplicación móvil OrdenARS. Complementa a [`ARQUITECTURA.md`](./ARQUITECTURA.md), que describe la
arquitectura y las decisiones de diseño.

---

## 1. Introducción

### 1.1 Propósito

OrdenARS es una aplicación Android nativa para el control de finanzas personales
en pesos argentinos. Su diferencial es la **detección automática de gastos**
leyendo las notificaciones que emiten las billeteras virtuales y bancos
argentinos: el usuario no tiene que cargar cada movimiento a mano.

### 1.2 Alcance

La aplicación:

- Detecta gastos automáticamente a partir de notificaciones de billeteras/bancos.
- Permite cargar gastos manualmente.
- Categoriza los gastos (automática por palabras clave, o manual).
- Muestra el gasto del mes, el historial, y estadísticas con gráficos.
- Permite fijar presupuestos mensuales por categoría y avisa al acercarse o
  superar el límite.
- Funciona 100 % offline: todos los datos viven en el dispositivo, sin backend,
  sin cuenta de usuario y sin conexión a internet.

Queda **fuera de alcance** (ver §6).

### 1.3 Definiciones

| Término | Significado |
|---|---|
| Gasto | Un egreso de dinero. Tiene monto, descripción, categoría, fecha y origen. |
| Origen | Cómo se registró el gasto: `MANUAL` (lo cargó el usuario) o `NOTIFICACION` (lo detectó la app). |
| Billetera | App de pago/banco cuyas notificaciones la app sabe leer (Personal Pay, Naranja X, Mercado Pago, etc.). |
| Categoría | Etiqueta de un gasto (Comida, Transporte, …). Tiene nombre, ícono (emoji), color y palabras clave. |
| Presupuesto | Límite de gasto para una categoría en un mes/año. |
| Alerta de presupuesto | Aviso que se dispara cuando una categoría cruza el 80 % o el 100 % de su presupuesto. |

### 1.4 Actores

- **Usuario final**: la única persona que usa la app. Instala, otorga permisos,
  carga gastos, configura categorías y presupuestos, consulta reportes.
- **Sistema operativo Android**: emite las notificaciones que la app escucha y
  gestiona los permisos (`BIND_NOTIFICATION_LISTENER_SERVICE`,
  `POST_NOTIFICATIONS`).

---

## 2. Requerimientos Funcionales

Convención de estado: ✅ implementado y probado · ⚠️ implementado, con pruebas
pendientes · ⬜ no implementado.

### 2.1 Onboarding

| ID | Requerimiento | Estado |
|---|---|---|
| RF-01 | En el primer arranque, la app muestra un onboarding de 3 pantallas que explica qué hace y cómo funciona la detección automática. | ✅ |
| RF-02 | El usuario puede saltar el onboarding o recorrerlo hasta el final. | ✅ |
| RF-03 | Al terminar el onboarding, la app ofrece abrir los Ajustes de Android para otorgar el permiso de acceso a notificaciones. | ✅ |
| RF-04 | El onboarding se muestra una sola vez; a partir del segundo arranque la app abre directamente en el inicio. | ✅ |

### 2.2 Detección automática de gastos

| ID | Requerimiento | Estado |
|---|---|---|
| RF-05 | La app escucha las notificaciones del sistema mediante un `NotificationListenerService`. | ✅ |
| RF-06 | Solo procesa notificaciones de una lista conocida de billeteras/bancos argentinos (24 apps, con package names verificados). Ignora cualquier otra app. | ✅ |
| RF-07 | Ante una notificación con el patrón "Pagaste $<monto> en <comercio>", la app registra el gasto automáticamente, sin intervención del usuario. | ⚠️ (pipeline probado por simulación; falta un ejemplo real confirmado de una billetera que mande el monto en el texto) |
| RF-08 | Ante una notificación de compra **sin monto** en el texto (p. ej. "Compraste en <comercio>"), la app emite una notificación propia ("Tocá para cargar el monto") que abre el formulario de carga con la categoría y la descripción ya precargadas. | ✅ (confirmado con notificaciones reales de Personal Pay) |
| RF-09 | El gasto detectado guarda el texto original de la notificación y el nombre de la billetera de origen. | ✅ |
| RF-10 | El usuario puede activar/desactivar la detección por cada billetera de forma individual, desde Configuración. | ✅ |
| RF-11 | Si el permiso de acceso a notificaciones no está otorgado, la app sigue siendo 100 % usable con carga manual; Configuración muestra el estado real del permiso y un acceso directo a otorgarlo. | ✅ |

### 2.3 Carga manual de gastos

| ID | Requerimiento | Estado |
|---|---|---|
| RF-12 | El usuario puede cargar un gasto manualmente indicando monto, categoría, descripción y fecha. | ✅ |
| RF-13 | El monto y la categoría son obligatorios; el botón de guardar permanece deshabilitado hasta que ambos estén completos. | ✅ |
| RF-14 | Si la descripción se deja vacía, se usa el nombre de la categoría como descripción. | ✅ |
| RF-15 | Tras guardar, la app confirma visualmente ("Gasto guardado") y vuelve al inicio. | ✅ |

### 2.4 Categorización

| ID | Requerimiento | Estado |
|---|---|---|
| RF-16 | Al detectar un gasto automáticamente, la app le asigna una categoría buscando coincidencias entre el texto del comercio y las palabras clave de cada categoría. | ✅ |
| RF-17 | Si ninguna categoría coincide, el gasto queda en la categoría por defecto "Otros". | ✅ |
| RF-18 | El usuario puede reasignar la categoría de cualquier gasto (al cargarlo manualmente, o desde el formulario precargado). | ✅ |

### 2.5 Editor de categorías

| ID | Requerimiento | Estado |
|---|---|---|
| RF-19 | El usuario puede crear una categoría nueva con nombre, ícono (emoji), color y palabras clave. | ✅ |
| RF-20 | El usuario puede editar el ícono, el color y las palabras clave de una categoría existente. | ✅ |
| RF-21 | El nombre de una categoría **no** se puede cambiar una vez creada (es su identificador y lo referencian gastos y presupuestos). | ✅ (decisión de diseño, ver §5) |
| RF-22 | No se puede crear una categoría con un nombre que ya existe; la app lo informa. | ✅ |
| RF-23 | El usuario puede eliminar una categoría que no tenga gastos cargados; al hacerlo se borran también sus presupuestos. | ✅ |
| RF-24 | No se puede eliminar la categoría "Otros" (la app la usa como fallback), ni una categoría que tenga gastos cargados; la app informa el motivo. | ✅ |

### 2.6 Historial

| ID | Requerimiento | Estado |
|---|---|---|
| RF-25 | La app lista todos los gastos, agrupados por fecha, del más reciente al más antiguo. | ✅ |
| RF-26 | El usuario puede buscar gastos por texto de la descripción. | ✅ |
| RF-27 | El usuario puede filtrar el historial por período (esta semana, este mes, últimos 3 meses, todos) y por categoría. | ✅ |
| RF-28 | El historial muestra el total y la cantidad de gastos del resultado filtrado. | ✅ |
| RF-29 | El usuario puede eliminar un gasto deslizándolo (swipe). | ✅ |
| RF-30 | Al tocar un gasto se abre una pantalla de detalle con monto, descripción, categoría, fecha completa y origen; si el gasto vino de una notificación, muestra además la billetera y el texto original. | ✅ |
| RF-31 | Desde el detalle, el usuario puede eliminar el gasto con confirmación previa. | ✅ |

### 2.7 Presupuestos

| ID | Requerimiento | Estado |
|---|---|---|
| RF-32 | El usuario puede fijar un presupuesto (monto máximo) para una categoría en el mes en curso. | ✅ |
| RF-33 | El usuario puede editar el monto de un presupuesto existente. | ✅ |
| RF-34 | El usuario puede eliminar un presupuesto, con confirmación previa; los gastos no se tocan. | ✅ |
| RF-35 | La pantalla de presupuestos muestra, por categoría con presupuesto: lo gastado, el límite, el porcentaje usado y una barra de progreso. | ✅ |
| RF-36 | Muestra también un resumen general: total presupuestado, total gastado y disponible. | ✅ |
| RF-37 | Cada categoría muestra un aviso visual a partir del 80 % ("Cerca del límite") y del 100 % ("Presupuesto superado") de su presupuesto. | ✅ |

### 2.8 Alertas de presupuesto

| ID | Requerimiento | Estado |
|---|---|---|
| RF-38 | Al registrar un gasto (manual o automático) que hace que una categoría cruce el 80 % o el 100 % de su presupuesto del mes, la app dispara una alerta. | ✅ |
| RF-39 | La alerta se muestra siempre in-app (aviso en Inicio y en Presupuestos) y además como notificación push del sistema si el permiso `POST_NOTIFICATIONS` está otorgado. | ✅ |
| RF-40 | La misma alerta (categoría + mes + umbral) no se repite dentro del mismo mes. | ✅ |
| RF-41 | El usuario puede activar/desactivar las alertas push desde Configuración. | ✅ |

### 2.9 Inicio (Dashboard) y estadísticas

| ID | Requerimiento | Estado |
|---|---|---|
| RF-42 | El inicio muestra el total gastado en el mes en curso. | ✅ |
| RF-43 | El inicio muestra un gráfico de torta de la distribución del gasto del mes por categoría, con leyenda. | ✅ |
| RF-44 | El inicio muestra el progreso de los presupuestos configurados y los últimos movimientos. | ✅ |
| RF-45 | Existe una pantalla de Estadísticas con un gráfico de torta y un gráfico de barras del gasto del mes por categoría. | ✅ |
| RF-46 | Los gráficos usan el color propio de cada categoría, de forma consistente entre la torta y las barras. | ✅ |
| RF-47 | Si no hay gastos en el mes, las secciones de gráficos no se muestran / muestran un estado vacío. | ✅ |

### 2.10 Configuración y datos

| ID | Requerimiento | Estado |
|---|---|---|
| RF-48 | El usuario puede exportar todo el historial de gastos a un archivo CSV. | ✅ |
| RF-49 | El usuario puede borrar todo el historial de gastos. | ✅ |
| RF-50 | Todas las preferencias (toggles de billeteras, permisos, flag de onboarding) persisten entre sesiones. | ✅ |
| RF-51 | Todos los datos (gastos, categorías, presupuestos) persisten localmente entre sesiones. | ✅ |

---

## 3. Requerimientos No Funcionales

### 3.1 Plataforma y tecnología

| ID | Requerimiento |
|---|---|
| RNF-01 | Aplicación Android nativa, escrita en Kotlin. |
| RNF-02 | Interfaz construida con Jetpack Compose y Material 3. |
| RNF-03 | Compatible desde Android 8.0 (API 26) en adelante. |
| RNF-04 | Arquitectura MVVM + Clean Architecture, con capas `data` / `domain` / `ui` / `service` separadas. |
| RNF-05 | Inyección de dependencias con Hilt. |
| RNF-06 | Persistencia con Room (base SQLite); preferencias con DataStore. |
| RNF-07 | Los gráficos se dibujan con las primitivas de Compose (`Canvas` / `drawArc`), sin librerías de terceros. |

### 3.2 Privacidad y seguridad

| ID | Requerimiento |
|---|---|
| RNF-08 | La app funciona 100 % offline: no realiza ninguna conexión de red, no tiene backend ni cuenta de usuario. |
| RNF-09 | Ningún dato del usuario sale del dispositivo. |
| RNF-10 | El `NotificationListenerService` solo lee el contenido de notificaciones de la lista blanca de billeteras conocidas; descarta el resto sin procesarlas. |
| RNF-11 | La app nunca solicita credenciales bancarias ni accede a otras apps; se limita a leer las notificaciones que el usuario ya recibe. |
| RNF-12 | El permiso de acceso a notificaciones y el de notificaciones push se piden de forma explícita y su ausencia degrada la funcionalidad de forma controlada (la carga manual sigue disponible). |

### 3.3 Usabilidad

| ID | Requerimiento |
|---|---|
| RNF-13 | Toda la interfaz y los textos están en español de Argentina. |
| RNF-14 | La app usa un único tema oscuro (dark-only), consistente en todas las pantallas. |
| RNF-15 | La navegación principal es una barra inferior de 3 ítems (Inicio, Agregar, Historial); Presupuestos, Configuración y Estadísticas se alcanzan desde Inicio. |
| RNF-16 | Toda acción destructiva (borrar gasto, categoría, presupuesto, historial) pide confirmación previa. |
| RNF-17 | Los formularios informan por qué un botón de guardar está deshabilitado y muestran los errores de validación en el lugar donde ocurren. |
| RNF-18 | Las transiciones y los gráficos usan animaciones sutiles para dar contexto sin distraer. |

### 3.4 Rendimiento y robustez

| ID | Requerimiento |
|---|---|
| RNF-19 | Toda operación de base de datos se ejecuta fuera del hilo principal (coroutines); la UI observa los datos de forma reactiva con `Flow` / `StateFlow`. |
| RNF-20 | El listener de notificaciones procesa cada notificación en tiempo constante (un parseo con expresiones regulares) y no bloquea el sistema. |
| RNF-21 | La app no asume el estado de los permisos: los verifica contra el sistema en cada arranque de la pantalla correspondiente. |
| RNF-22 | El esquema de la base de datos está versionado para permitir migraciones futuras sin pérdida de datos. |

### 3.5 Mantenibilidad y calidad

| ID | Requerimiento |
|---|---|
| RNF-23 | El código sigue una estructura de paquetes por capa y por pantalla, con nombres en español coherentes con el dominio. |
| RNF-24 | Los componentes de UI reutilizables (tarjetas, botones, avisos, gráficos) viven en `ui/components` y se comparten entre pantallas. |
| RNF-25 | Cada avance del desarrollo se registra en una bitácora, con las decisiones tomadas y los problemas encontrados. |
| RNF-26 | Cada módulo se somete a pruebas funcionales manuales (emulador y teléfono físico) antes de darse por terminado; los resultados y los bugs encontrados quedan documentados. |
| RNF-27 | La lógica de negocio (parser de notificaciones, categorización, alertas de presupuesto y repositorios) está cubierta por tests unitarios automatizados que corren en CI en cada push y pull request. |

---

## 4. Interfaces externas

- **Notificaciones de Android**: entrada. La app recibe `StatusBarNotification` de
  otras apps a través del `NotificationListenerService` y lee `title` / `text` /
  `bigText`.
- **Ajustes de Android**: la app deriva al usuario a
  *Ajustes → Acceso a notificaciones* y a los ajustes de notificación de la
  propia app para gestionar permisos.
- **Sistema de archivos**: salida. La exportación a CSV escribe un archivo que el
  usuario puede compartir.
- **No hay** interfaz de red, API, ni servicio en la nube.

---

## 5. Restricciones y decisiones de diseño

- **Sin backend, sin login, sin red** — es una restricción de producto, no una
  limitación técnica. Toda la lógica y los datos son locales.
- **El nombre de una categoría es su clave primaria** y no se puede renombrar:
  gastos y presupuestos la referencian como texto suelto; permitir el renombrado
  habría implicado una cascada manual sobre todas las tablas.
- **La detección automática solo puede leer lo que la billetera pone en la
  notificación.** Si una billetera no incluye el monto en el texto (caso real de
  Personal Pay), la app no puede inventarlo: por eso el flujo de "compra
  detectada sin monto" (RF-08) que le pide el monto al usuario.
- **`POST_NOTIFICATIONS` solo se puede solicitar desde una Activity**, nunca desde
  un `Service`; por eso el `NotificationListenerService` solo verifica permisos y
  el pedido vive en la pantalla de Configuración.
- **Simular notificaciones de terceros no es posible** sin un dispositivo con esas
  apps: `adb` siempre postea como `com.android.shell`. Las pruebas end-to-end de
  la detección se hicieron en el teléfono físico del usuario.
- **Tema oscuro único**: decisión de diseño; no hay tema claro.

---

## 6. Fuera de alcance

- Registro de **ingresos** (la app solo trackea egresos; la tarjeta de inicio
  muestra "Ingresos: $0" como placeholder).
- Múltiples usuarios o perfiles.
- Sincronización entre dispositivos / respaldo en la nube.
- Importación automática de resúmenes o CSV bancarios (se evaluó y se descartó
  por requerir credenciales bancarias y romper el enfoque offline).
- Presupuestos de meses distintos al mes en curso desde la UI (el selector de mes
  existe pero es de solo lectura).
- Categorización con IA / aprendizaje automático (la categorización es por
  palabras clave).

---

## 7. Trazabilidad (requerimiento → implementación)

| Módulo | Pantalla / componente | Archivos principales |
|---|---|---|
| Onboarding | Onboarding | `ui/screens/onboarding/`, `PreferenciasRepository` |
| Detección automática | — (servicio) | `service/OrdenARSNotificationListener.kt`, `service/NotificationParser.kt`, `service/NotificadorCompraDetectada.kt`, `domain/usecase/CategorizarGastoUseCase.kt` |
| Carga manual | Agregar Gasto | `ui/screens/agregar/`, `domain/usecase/RegistrarGastoUseCase.kt` |
| Categorías | Editor de categorías | `ui/screens/categorias/`, `domain/usecase/EliminarCategoriaUseCase.kt` |
| Historial | Historial + Detalle de gasto | `ui/screens/historial/`, `ui/screens/detalle/` |
| Presupuestos | Presupuestos | `ui/screens/presupuesto/` |
| Alertas de presupuesto | — (use case + notificador) | `domain/usecase/VerificarAlertaPresupuestoUseCase.kt`, `service/NotificadorAlertasPresupuesto.kt`, `ui/components/Components.kt` (`AvisoPresupuesto`) |
| Inicio + estadísticas | Dashboard + Estadísticas | `ui/screens/dashboard/`, `ui/screens/estadisticas/`, `ui/components/Charts.kt` |
| Configuración / datos | Configuración | `ui/screens/configuracion/` |
| Persistencia | — | `data/local/` (Room), `data/repository/`, DataStore en `PreferenciasRepository` |

---

## 8. Estado de cumplimiento (2026-08-28)

- **Requerimientos funcionales**: 49 de 51 ✅, 2 ⚠️ (RF-07 detección con monto en
  el texto — pipeline listo, falta un caso real; y ninguno ⬜).
- **Requerimientos no funcionales**: todos cumplidos. Desde 2026-10-27 además de
  las pruebas funcionales manuales (RNF-26) hay una suite de **50 tests
  unitarios** (RNF-27) que corre en GitHub Actions; al escribirla aparecieron y
  se corrigieron 3 bugs (ver la sección *Tests* del README).
- **Código publicado** en GitHub con README, licencia MIT y build automático (GitHub Actions).
