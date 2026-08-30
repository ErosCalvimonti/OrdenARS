package com.calvimonti.ordenars.ui.screens.categorias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calvimonti.ordenars.domain.model.Categoria
import com.calvimonti.ordenars.ui.components.GradientButton
import com.calvimonti.ordenars.ui.components.OrdenARSCard
import com.calvimonti.ordenars.ui.theme.Alerta
import com.calvimonti.ordenars.ui.theme.FondoCard
import com.calvimonti.ordenars.ui.theme.FondoPrincipal
import com.calvimonti.ordenars.ui.theme.FondoSecundario
import com.calvimonti.ordenars.ui.theme.Primario
import com.calvimonti.ordenars.ui.theme.TextoPrimario
import com.calvimonti.ordenars.ui.theme.TextoSecundario
import com.calvimonti.ordenars.ui.theme.TextoTerciario

private val PALETA_COLORES = listOf(
    "#FFB347", "#00D4AA", "#6C63FF", "#4FC3F7", "#FF6B6B",
    "#9E9E9E", "#2D9CDB", "#8C1B1B", "#FFD166", "#A78BFA"
)

@Composable
fun CategoriasScreen(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit = {},
    viewModel: CategoriasViewModel = hiltViewModel()
) {
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    val mensajeError by viewModel.mensaje.collectAsStateWithLifecycle()

    var categoriaEnEdicion by remember { mutableStateOf<Categoria?>(null) }
    var mostrarHoja by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = FondoPrincipal,
        topBar = {
            EncabezadoCategorias(
                onVolver = onVolver,
                onAgregar = {
                    categoriaEnEdicion = null
                    mostrarHoja = true
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categorias, key = { it.nombre }) { categoria ->
                FilaCategoria(
                    categoria = categoria,
                    onClick = {
                        categoriaEnEdicion = categoria
                        mostrarHoja = true
                    }
                )
            }
        }
    }

    if (mostrarHoja) {
        HojaCategoria(
            categoriaInicial = categoriaEnEdicion,
            mensajeError = mensajeError,
            onErrorVisto = viewModel::ackMensaje,
            onGuardar = { categoria ->
                viewModel.ackMensaje()
                if (categoriaEnEdicion != null) {
                    viewModel.actualizarCategoria(categoria) { mostrarHoja = false }
                } else {
                    viewModel.crearCategoria(categoria) { mostrarHoja = false }
                }
            },
            onEliminar = { nombre ->
                viewModel.ackMensaje()
                viewModel.eliminarCategoria(nombre) { mostrarHoja = false }
            },
            onDismiss = {
                viewModel.ackMensaje()
                mostrarHoja = false
            }
        )
    }
}

@Composable
private fun EncabezadoCategorias(onVolver: () -> Unit, onAgregar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(FondoPrincipal)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = onVolver, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoPrimario)
        }
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.headlineMedium,
            color = TextoPrimario,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onAgregar,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clip(CircleShape)
                .background(Primario)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva categoría", tint = TextoPrimario)
        }
    }
}

@Composable
private fun FilaCategoria(categoria: Categoria, onClick: () -> Unit) {
    OrdenARSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colorDesdeHex(categoria.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(categoria.icono, style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(categoria.nombre, style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
                Text(
                    text = if (categoria.palabrasClave.isEmpty()) {
                        "Sin palabras clave"
                    } else {
                        "${categoria.palabrasClave.size} palabra(s) clave"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextoSecundario)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HojaCategoria(
    categoriaInicial: Categoria?,
    mensajeError: String?,
    onErrorVisto: () -> Unit,
    onGuardar: (Categoria) -> Unit,
    onEliminar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val esNueva = categoriaInicial == null
    val estadoHoja = rememberModalBottomSheetState()
    var nombre by remember { mutableStateOf(categoriaInicial?.nombre ?: "") }
    var icono by remember { mutableStateOf(categoriaInicial?.icono ?: "") }
    var colorSeleccionado by remember { mutableStateOf(categoriaInicial?.color ?: PALETA_COLORES.first()) }
    var palabras by remember { mutableStateOf(categoriaInicial?.palabrasClave ?: emptyList()) }
    var campoPalabra by remember { mutableStateOf("") }
    var mostrarConfirmarBorrado by remember { mutableStateOf(false) }

    val teclado = LocalSoftwareKeyboardController.current
    val focoActual = LocalFocusManager.current
    val tecladoVisible = WindowInsets.isImeVisible
    BackHandler(enabled = tecladoVisible) {
        teclado?.hide()
        focoActual.clearFocus()
    }

    fun agregarPalabra() {
        val palabra = campoPalabra.trim()
        if (palabra.isNotEmpty() && palabra !in palabras) {
            palabras = palabras + palabra
        }
        campoPalabra = ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = estadoHoja,
        containerColor = FondoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (esNueva) "Nueva categoría" else "Editar categoría",
                style = MaterialTheme.typography.headlineMedium,
                color = TextoPrimario
            )
            Spacer(Modifier.height(20.dp))

            Text("Nombre", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            Spacer(Modifier.height(8.dp))
            if (esNueva) {
                TextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it.take(24)
                        if (mensajeError != null) onErrorVisto()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Mascotas", color = TextoTerciario) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FondoSecundario,
                        unfocusedContainerColor = FondoSecundario,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextoPrimario,
                        unfocusedTextColor = TextoPrimario,
                        cursorColor = Primario
                    )
                )
            } else {
                Text(nombre, style = MaterialTheme.typography.bodyLarge, color = TextoPrimario)
                Text(
                    "El nombre no se puede cambiar una vez creada",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoTerciario
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Ícono", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            Spacer(Modifier.height(8.dp))
            TextField(
                value = icono,
                onValueChange = { icono = it.take(8) },
                modifier = Modifier.width(90.dp),
                placeholder = { Text("🐾", color = TextoTerciario) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FondoSecundario,
                    unfocusedContainerColor = FondoSecundario,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextoPrimario,
                    unfocusedTextColor = TextoPrimario,
                    cursorColor = Primario
                )
            )

            Spacer(Modifier.height(20.dp))
            Text("Color", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PALETA_COLORES.forEach { hex ->
                    val seleccionado = hex == colorSeleccionado
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorDesdeHex(hex))
                            .clickable { colorSeleccionado = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (seleccionado) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = TextoPrimario, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Palabras clave", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            Text(
                "Si una notificación menciona alguna, el gasto se categoriza acá solo",
                style = MaterialTheme.typography.bodySmall,
                color = TextoTerciario
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = campoPalabra,
                    onValueChange = { campoPalabra = it.take(30) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ej: Rappi", color = TextoTerciario) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FondoSecundario,
                        unfocusedContainerColor = FondoSecundario,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextoPrimario,
                        unfocusedTextColor = TextoPrimario,
                        cursorColor = Primario
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = ::agregarPalabra,
                    modifier = Modifier.clip(CircleShape).background(Primario)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar palabra clave", tint = TextoPrimario)
                }
            }
            if (palabras.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    palabras.forEach { palabra ->
                        ChipPalabraClave(texto = palabra, onEliminar = { palabras = palabras - palabra })
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            if (mensajeError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Alerta.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Alerta,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(mensajeError, style = MaterialTheme.typography.bodySmall, color = Alerta)
                }
                Spacer(Modifier.height(12.dp))
            }

            val formularioValido = nombre.isNotBlank() && icono.isNotBlank()
            GradientButton(
                texto = "Guardar",
                habilitado = formularioValido,
                onClick = {
                    onGuardar(
                        Categoria(
                            nombre = nombre.trim(),
                            icono = icono.trim(),
                            color = colorSeleccionado,
                            palabrasClave = palabras
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (!formularioValido) {
                Spacer(Modifier.height(8.dp))
                val faltante = when {
                    nombre.isBlank() && icono.isBlank() -> "Escribí un nombre y elegí un ícono para guardar"
                    nombre.isBlank() -> "Escribí un nombre para guardar"
                    else -> "Elegí un ícono (un emoji) para guardar"
                }
                Text(
                    text = faltante,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoTerciario,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!esNueva && nombre != "Otros") {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { mostrarConfirmarBorrado = true }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Eliminar categoría", style = MaterialTheme.typography.labelMedium, color = Alerta)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (mostrarConfirmarBorrado) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarBorrado = false },
            title = { Text("¿Eliminar \"$nombre\"?") },
            text = { Text("Se borra la categoría y sus presupuestos configurados. Si tiene gastos cargados, no se va a poder borrar.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmarBorrado = false
                    onEliminar(nombre)
                }) {
                    Text("Eliminar", color = Alerta)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ChipPalabraClave(texto: String, onEliminar: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(FondoSecundario)
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, style = MaterialTheme.typography.labelMedium, color = TextoPrimario)
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Quitar $texto",
            tint = TextoSecundario,
            modifier = Modifier.size(16.dp).clip(CircleShape).clickable(onClick = onEliminar)
        )
    }
}

private fun colorDesdeHex(hex: String): Color = Color(android.graphics.Color.parseColor(hex))
