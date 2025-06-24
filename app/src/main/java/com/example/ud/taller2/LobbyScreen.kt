package com.example.ud.taller2

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ud.taller2.model.Partida
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LobbyScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance().reference

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

        // BOTÓN CREAR PARTIDA
        Button(
            onClick = {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                generarCodigoUnico(
                    db = db,
                    onSuccess = { codigo ->
                        val partida = Partida(jugador1 = uid)
                        db.child("partidas").child(codigo).setValue(partida)
                            .addOnSuccessListener {
                                codigoGenerado = codigo
                                mostrarDialogo = true
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al crear partida", Toast.LENGTH_SHORT).show()
                            }
                    },
                    onError = {
                        Toast.makeText(context, "Error al verificar código", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Partida")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CAMPO PARA UNIRSE
        OutlinedTextField(
            value = codigoPartida,
            onValueChange = { codigoPartida = it },
            label = { Text("Código de partida") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // BOTÓN UNIRSE
        Button(
            onClick = {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Partida llena o inválida", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Código no válido", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Error al consultar Firebase", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unirse a Partida")
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
            Text("Cerrar Sesión")
        }
    }

    // DIÁLOGO PARA MOSTRAR CÓDIGO GENERADO
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {
                Button(onClick = {
                    mostrarDialogo = false
                    navController.navigate("juego/$codigoGenerado")
                }) {
                    Text("Ir al juego")
                }
            },
            title = { Text("Código de Partida") },
            text = { Text("Tu código es: $codigoGenerado\nCompártelo con el otro jugador.") }
        )
    }
}

// FUNCIÓN QUE GENERA UN CÓDIGO ÚNICO
fun generarCodigoUnico(
    db: DatabaseReference,
    onSuccess: (String) -> Unit,
    onError: () -> Unit
) {
    fun intentarGenerar() {
        val nuevoCodigo = generarCodigo()
        db.child("partidas").child(nuevoCodigo).get().addOnSuccessListener {
            if (!it.exists()) {
                onSuccess(nuevoCodigo)
            } else {
                intentarGenerar()
            }
        }.addOnFailureListener {
            onError()
        }
    }

    intentarGenerar()
}
