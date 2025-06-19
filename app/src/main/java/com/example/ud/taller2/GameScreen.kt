package com.ud.taller2

import kotlinx.coroutines.delay
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

@Composable
fun GameScreen(modifier: Modifier = Modifier, navController: NavController) {
    val rows = 6
    val cols = 7
    val context = LocalContext.current

    var board by remember { mutableStateOf(List(rows) { MutableList(cols) { 0 } }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var triggerMachineTurn by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf(0) }
    var showDrawMessage by remember { mutableStateOf(false) }

    fun checkWin(player: Int): Boolean {
        for (r in 0 until rows) for (c in 0..cols - 4)
            if ((0..3).all { board[r][c + it] == player }) return true
        for (c in 0 until cols) for (r in 0..rows - 4)
            if ((0..3).all { board[r + it][c] == player }) return true
        for (r in 0..rows - 4) for (c in 0..cols - 4)
            if ((0..3).all { board[r + it][c + it] == player }) return true
        for (r in 3 until rows) for (c in 0..cols - 4)
            if ((0..3).all { board[r - it][c + it] == player }) return true
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
        }
    }

    fun isDraw(): Boolean = (winner == 0 && board.all { row -> row.none { it == 0 } })

    fun resetGame() {
        board = List(rows) { MutableList(cols) { 0 } }
        isPlayerTurn = true
        triggerMachineTurn = false
        winner = 0
        showDrawMessage = false
    }

    LaunchedEffect(triggerMachineTurn) {
        if (triggerMachineTurn && winner == 0) {
            delay(600)
            machineMove()
            triggerMachineTurn = false
        }
    }

    LaunchedEffect(board) {
        if (isDraw()) {
            showDrawMessage = true
            Toast.makeText(context, "Draw!", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                winner == 1 -> "¡Ganaste!"
                winner == 2 -> "¡La máquina gana!"
                showDrawMessage -> "¡Empate!"
                else -> "Turno: ${if (isPlayerTurn) "Jugador" else "Máquina"}"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
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
                                enabled = (cell == 0 && isPlayerTurn && winner == 0 && !showDrawMessage)
                            ) {
                                if (dropPiece(col, 1)) {
                                    isPlayerTurn = false
                                    triggerMachineTurn = true
                                }
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { resetGame() }) {
            Text("Reiniciar Juego")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔁 Botón para cerrar sesión
        OutlinedButton(onClick = {
            navController.navigate("login") {
                popUpTo("bienvenida") { inclusive = true }
            }
        }) {
            Text("Cerrar Sesión")
        }
    }
}
