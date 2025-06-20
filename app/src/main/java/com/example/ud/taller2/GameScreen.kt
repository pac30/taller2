package com.ud.taller2

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ud.taller2.model.Palabra
import com.example.ud.taller2.model.Partida
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun GameScreen(navController: NavController, codigoPartida: String) {
    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().reference
    val partidaRef = db.child("partidas").child(codigoPartida)
    val palabrasRef = db.child("palabras")
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var partida by remember { mutableStateOf(Partida()) }
    var winner by remember { mutableStateOf(0) }
    var showDraw by remember { mutableStateOf(false) }

    var palabraActual by remember { mutableStateOf<Palabra?>(null) }
    var respuestaUsuario by remember { mutableStateOf("") }
    var mostrarPregunta by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    // Escuchar cambios en la partida
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val actual = snapshot.getValue(Partida::class.java)
                if (actual != null) {
                    partida = actual
                    winner = checkWin(actual.tablero)
                    showDraw = isDraw(actual.tablero) && winner == 0
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }

        partidaRef.addValueEventListener(listener)
        onDispose {
            partidaRef.removeEventListener(listener)
        }
    }

    val isTurnoJugador = (uid == partida.jugador1 && partida.turno == 1) ||
            (uid == partida.jugador2 && partida.turno == 2)

    // Cargar palabra
    LaunchedEffect(isTurnoJugador, palabraActual) {
        if (isTurnoJugador && palabraActual == null && mostrarPregunta) {
            palabrasRef.get().addOnSuccessListener { snapshot ->
                val palabras = snapshot.children.mapNotNull { it.getValue(Palabra::class.java) }
                if (palabras.isNotEmpty()) {
                    palabraActual = palabras.random()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                winner == 1 -> "¡Ganó Jugador 1!"
                winner == 2 -> "¡Ganó Jugador 2!"
                showDraw -> "¡Empate!"
                else -> if (isTurnoJugador) "Tu turno" else "Turno del oponente"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        // Pregunta de traducción
        if (isTurnoJugador && palabraActual != null && mostrarPregunta) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Traduce: ${palabraActual!!.esp}", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = respuestaUsuario,
                onValueChange = { respuestaUsuario = it },
                label = { Text("Traducción en inglés") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                if (respuestaUsuario.trim().equals(palabraActual!!.eng, ignoreCase = true)) {
                    mensajeError = ""
                    mostrarPregunta = false
                } else {
                    mensajeError = "❌ Incorrecto. Turno perdido."
                    val nuevoTurno = if (partida.turno == 1) 2 else 1
                    partidaRef.child("turno").setValue(nuevoTurno)
                    palabraActual = null
                    mostrarPregunta = true
                }
                respuestaUsuario = ""
            }) {
                Text("Verificar")
            }

            if (mensajeError.isNotEmpty()) {
                Text(mensajeError, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Tablero
        partida.tablero.forEachIndexed { rowIndex, fila ->
            Row {
                fila.forEachIndexed { colIndex, celda ->
                    val color = when (celda) {
                        1 -> Color(0xFF4CAF50)
                        2 -> Color(0xFFE53935)
                        else -> Color(0xFFBDBDBD)
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(40.dp)
                            .background(color)
                            .clickable(
                                enabled = celda == 0 && winner == 0 && isTurnoJugador && !mostrarPregunta
                            ) {
                                hacerMovimiento(
                                    partidaRef,
                                    rowIndex,
                                    colIndex,
                                    partida,
                                    uid
                                )
                                palabraActual = null
                                mostrarPregunta = true
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val nuevo = Partida(
                jugador1 = partida.jugador1,
                jugador2 = partida.jugador2
            )
            partidaRef.setValue(nuevo)
            palabraActual = null
            mostrarPregunta = true
            respuestaUsuario = ""
            mensajeError = ""
        }) {
            Text("Reiniciar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = {
            navController.popBackStack()
        }) {
            Text("Salir")
        }
    }
}

// Movimiento
fun hacerMovimiento(
    ref: DatabaseReference,
    fila: Int,
    columna: Int,
    actual: Partida,
    uid: String?
) {
    if (uid == null) return
    val tablero = actual.tablero.map { it.toMutableList() }.toMutableList()

    for (r in (tablero.size - 1) downTo 0) {
        if (tablero[r][columna] == 0) {
            tablero[r][columna] = actual.turno
            break
        }
    }

    val nuevoTurno = if (actual.turno == 1) 2 else 1

    val actualizada = actual.copy(
        tablero = tablero,
        turno = nuevoTurno
    )
    ref.setValue(actualizada)
}

// Verificación de victoria
fun checkWin(board: List<List<Int>>): Int {
    val rows = board.size
    val cols = board[0].size

    fun checkDir(r: Int, c: Int, dr: Int, dc: Int): Int {
        val player = board[r][c]
        if (player == 0) return 0
        for (i in 1..3) {
            val nr = r + dr * i
            val nc = c + dc * i
            if (nr !in 0 until rows || nc !in 0 until cols || board[nr][nc] != player) return 0
        }
        return player
    }

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            checkDir(r, c, 0, 1).takeIf { it != 0 }?.let { return it }
            checkDir(r, c, 1, 0).takeIf { it != 0 }?.let { return it }
            checkDir(r, c, 1, 1).takeIf { it != 0 }?.let { return it }
            checkDir(r, c, -1, 1).takeIf { it != 0 }?.let { return it }
        }
    }

    return 0
}

fun isDraw(board: List<List<Int>>): Boolean {
    return board.all { row -> row.none { it == 0 } }
}