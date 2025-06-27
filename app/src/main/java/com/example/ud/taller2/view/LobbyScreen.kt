package com.example.ud.taller2.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ud.taller2.model.Partida
import com.example.ud.taller2.repository.GameRepository
import com.example.ud.taller2.viewmodel.GameViewModel
import com.example.ud.taller2.viewmodel.GameViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LobbyScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance().reference
    val repository = remember { GameRepository(FirebaseDatabase.getInstance().reference) } // ✅ Correct

    // Only used to generate code, not to listen to a game yet
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(repository, "")
    )

    var codigoPartida by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var codigoGenerado by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lobby", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                repository.generarCodigoUnico(
                    onSuccess = { codigo ->
                        val partida = Partida(jugador1 = uid)
                        db.child("partidas").child(codigo).setValue(partida)
                            .addOnSuccessListener {
                                codigoGenerado = codigo
                                mostrarDialogo = true
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error creating game", Toast.LENGTH_SHORT).show()
                            }
                    },
                    onError = {
                        Toast.makeText(context, "Error generating code", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Game")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = codigoPartida,
            onValueChange = { codigoPartida = it },
            label = { Text("Game Code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val codigo = codigoPartida.trim().uppercase()
                val ref = db.child("partidas").child(codigo)

                ref.get().addOnSuccessListener { data ->
                    if (data.exists()) {
                        val partida = data.getValue(Partida::class.java)
                        if (partida?.jugador2?.isEmpty() == true && partida.jugador1 != uid) {
                            ref.child("jugador2").setValue(uid)
                            navController.navigate("juego/$codigo")
                        } else {
                            Toast.makeText(context, "Game full or invalid", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Invalid code", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Firebase error", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join Game")
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo("lobby") { inclusive = true }
                }
            }
        ) {
            Text("Log Out")
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {
                Button(onClick = {
                    mostrarDialogo = false
                    navController.navigate("juego/$codigoGenerado")
                }) {
                    Text("Go to Game")
                }
            },
            title = { Text("Game Code") },
            text = { Text("Your code is: $codigoGenerado\nShare it with the other player.") }
        )
    }
}