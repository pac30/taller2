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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay

@Composable
fun GameScreen(navController: NavController, codigoPartida: String = "") {
    val rows = 6
    val cols = 7
    val context = LocalContext.current

    // Firebase
    val db = FirebaseDatabase.getInstance().reference

    // Estado del juego
    var board by remember { mutableStateOf(List(rows) { MutableList(cols) { 0 } }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var triggerMachineTurn by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf(0) }
    var showDrawMessage by remember { mutableStateOf(false) }

    // Traducción
    var palabraActual by remember { mutableStateOf<Palabra?>(null) }
    var respuestaUsuario by remember { mutableStateOf("") }
    var mostrarPregunta by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    fun cargarPalabraAleatoria() {
        db.child("palabras").get().addOnSuccessListener { snapshot ->
            val palabras = snapshot.children.mapNotNull { it.getValue(Palabra::class.java) }
            if (palabras.isNotEmpty()) {
                palabraActual = palabras.random()
            }
        }
    }

    fun checkWin(player: Int): Boolean {
        for (r in 0 until rows) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r][c + it] == player }) return true
            }
        }
        for (c in 0 until cols) {
            for (r in 0..rows - 4) {
                if ((0..3).all { board[r + it][c] == player }) return true
            }
        }
        for (r in 0..rows - 4) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r + it][c + it] == player }) return true
            }
        }
        for (r in 3 until rows) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r - it][c + it] == player }) return true
            }
        }
        return false
    }

    fun dropPiece(column: Int, player: Int): Boolean {
        for (row in (rows - 1) downTo 0) {
            if (board[row][column] == 0) {
                board = board.toMutableList().apply {
                    this[row] = this[row].toMutableList().apply { this[column] = player }
                }
                if (checkWin(player)) winner = player
                return true
            }
        }
        return false
    }

    fun machineMove() {
        val availableColumns = (0 until cols).filter { board[0][it] == 0 }
        if (availableColumns.isNotEmpty()) {
            val randomColumn = availableColumns.random()
            dropPiece(randomColumn, 2)
            isPlayerTurn = true
            palabraActual = null
            mostrarPregunta = true
        }
    }

    fun isDraw(): Boolean = (winner == 0 && board.all { row -> row.none { it == 0 } })

    fun resetGame() {
        board = List(rows) { MutableList(cols) { 0 } }
        isPlayerTurn = true
        triggerMachineTurn = false
        winner = 0
        showDrawMessage = false
        palabraActual = null
        mostrarPregunta = true
        respuestaUsuario = ""
        mensajeError = ""
    }

    // Cargar palabra al iniciar turno
    if (isPlayerTurn && palabraActual == null && winner == 0 && mostrarPregunta) {
        cargarPalabraAleatoria()
    }

    LaunchedEffect(triggerMachineTurn) {
        if (triggerMachineTurn && winner == 0) {
            delay(800)
            machineMove()
            triggerMachineTurn = false
        }
    }

    LaunchedEffect(board) {
        if (isDraw()) {
            showDrawMessage = true
            Toast.makeText(context, "¡Empate!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                winner == 1 -> "¡Jugador gana!"
                winner == 2 -> "¡Máquina gana!"
                showDrawMessage -> "¡Empate!"
                else -> "Turno: ${if (isPlayerTurn) "Jugador" else "Máquina"}"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        if (mostrarPregunta && palabraActual != null) {
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
                    mostrarPregunta = false
                    mensajeError = ""
                } else {
                    isPlayerTurn = false
                    triggerMachineTurn = true
                    palabraActual = null
                    mostrarPregunta = true
                    respuestaUsuario = ""
                    mensajeError = "❌ Respuesta incorrecta. Turno perdido."
                }
            }) {
                Text("Verificar")
            }

            if (mensajeError.isNotEmpty()) {
                Text(mensajeError, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                for (col in 0 until cols) {
                    val cell = board[row][col]
                    val color = when (cell) {
                        1 -> Color(0xFF4CAF50)
                        2 -> Color(0xFFE53935)
                        else -> Color(0xFFBDBDBD)
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(40.dp)
                            .background(color, shape = MaterialTheme.shapes.medium)
                            .clickable(
                                enabled = (cell == 0 && isPlayerTurn && winner == 0 && !showDrawMessage && !mostrarPregunta)
                            ) {
                                if (dropPiece(col, 1)) {
                                    isPlayerTurn = false
                                    triggerMachineTurn = true
                                    palabraActual = null
                                    mostrarPregunta = true
                                    respuestaUsuario = ""
                                }
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { resetGame() }) {
            Text("Reiniciar juego")
        }
    }
}