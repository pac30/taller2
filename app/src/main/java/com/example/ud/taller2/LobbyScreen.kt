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
import com.example.ud.taller2.model.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LobbyScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance().reference

    var gameCode by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var generatedCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lobby", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // CREATE GAME BUTTON
        Button(
            onClick = {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                generateUniqueCode(
                    db = db,
                    onSuccess = { code ->
                        val game = Game(player1 = uid, turn = 1) // ✅ Player 1 (you) starts
                        db.child("games").child(code).setValue(game)
                            .addOnSuccessListener {
                                generatedCode = code
                                showDialog = true
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to create game", Toast.LENGTH_SHORT).show()
                            }
                    },
                    onError = {
                        Toast.makeText(context, "Failed to validate code", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Game")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // JOIN GAME TEXT FIELD
        OutlinedTextField(
            value = gameCode,
            onValueChange = { gameCode = it },
            label = { Text("Game code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // JOIN GAME BUTTON
        Button(
            onClick = {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val code = gameCode.trim().uppercase()
                val ref = db.child("games").child(code)

                ref.get().addOnSuccessListener { data ->
                    if (data.exists()) {
                        val game = data.getValue(Game::class.java)
                        if (game?.player2?.isEmpty() == true && game.player1 != uid) {
                            ref.child("player2").setValue(uid)
                            navController.navigate("game/$code")
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

    // DIALOG TO SHOW GENERATED GAME CODE
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    navController.navigate("game/$generatedCode")
                }) {
                    Text("Go to Game")
                }
            },
            title = { Text("Game Code") },
            text = { Text("Your game code is: $generatedCode\nShare it with your opponent.") }
        )
    }
}

// FUNCTION TO GENERATE UNIQUE GAME CODE
fun generateUniqueCode(
    db: DatabaseReference,
    onSuccess: (String) -> Unit,
    onError: () -> Unit
) {
    fun tryGenerate() {
        val newCode = generateCode()
        db.child("games").child(newCode).get().addOnSuccessListener {
            if (!it.exists()) {
                onSuccess(newCode)
            } else {
                tryGenerate()
            }
        }.addOnFailureListener {
            onError()
        }
    }

    tryGenerate()
}

// SIMPLE CODE GENERATOR FUNCTION
fun generateCode(): String {
    val chars = ('A'..'Z')
    return (1..6).map { chars.random() }.joinToString("")
}