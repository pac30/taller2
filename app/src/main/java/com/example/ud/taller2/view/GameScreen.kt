package com.example.ud.taller2.view

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
import com.example.ud.taller2.viewmodel.GameViewModel

@Composable
fun GameScreen(navController: NavController, viewModel: GameViewModel) {
    val context = LocalContext.current

    val partida by viewModel.partida.collectAsState()
    val palabraActual by viewModel.palabraActual.collectAsState()
    val respuestaUsuario by viewModel.respuestaUsuario.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val mostrarPregunta by viewModel.mostrarPregunta.collectAsState()
    val winner by viewModel.winner.collectAsState()
    val showDraw by viewModel.showDraw.collectAsState()
    val isTurnoJugador by viewModel.isTurnoJugador.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                winner == 1 -> "Player 1 wins!"
                winner == 2 -> "Player 2 wins!"
                showDraw -> "Draw!"
                else -> if (isTurnoJugador) "Your turn" else "Opponent's turn"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        // Show translation question if it's the player's turn and a word is available
        if (isTurnoJugador && palabraActual != null && mostrarPregunta) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Translate: ${palabraActual!!.esp}", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = respuestaUsuario,
                onValueChange = { viewModel.onRespuestaChange(it) },
                label = { Text("English translation") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                viewModel.verificarRespuesta()
            }) {
                Text("Check")
            }

            if (mensajeError.isNotEmpty()) {
                Text(mensajeError, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Draw the board
        partida.tablero.forEachIndexed { _, fila ->
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
                                viewModel.hacerMovimiento(colIndex)
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = {
            navController.popBackStack()
        }) {
            Text("Exit")
        }
    }
}