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
import com.example.ud.taller2.model.Word
import com.example.ud.taller2.model.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun GameScreen(navController: NavController, gameCode: String) {
    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().reference
    val gameRef = db.child("games").child(gameCode)
    val wordsRef = db.child("words")
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var game by remember { mutableStateOf(Game()) }
    var winner by remember { mutableStateOf(0) }
    var showDraw by remember { mutableStateOf(false) }

    var currentWord by remember { mutableStateOf<Word?>(null) }
    var userAnswer by remember { mutableStateOf("") }
    var showQuestion by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Listen for game state changes
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updated = snapshot.getValue(Game::class.java)
                if (updated != null) {
                    game = updated
                    winner = checkWinner(updated.board)
                    showDraw = isDraw(updated.board) && winner == 0
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show()
            }
        }

        gameRef.addValueEventListener(listener)
        onDispose {
            gameRef.removeEventListener(listener)
        }
    }

    val isPlayerTurn = (uid == game.player1 && game.turn == 1) ||
            (uid == game.player2 && game.turn == 2)

    // Load word
    LaunchedEffect(isPlayerTurn, currentWord) {
        if (isPlayerTurn && currentWord == null && showQuestion) {
            wordsRef.get().addOnSuccessListener { snapshot ->
                val words = snapshot.children.mapNotNull { it.getValue(Word::class.java) }
                if (words.isNotEmpty()) {
                    currentWord = words.random()
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
                winner == 1 -> "Player 1 wins!"
                winner == 2 -> "Player 2 wins!"
                showDraw -> "Draw!"
                else -> if (isPlayerTurn) "Your turn" else "Opponent's turn"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        // Translation challenge
        if (isPlayerTurn && currentWord != null && showQuestion) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Translate: ${currentWord!!.spanish}", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                label = { Text("English translation") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                if (userAnswer.trim().equals(currentWord!!.english, ignoreCase = true)) {
                    errorMessage = ""
                    showQuestion = false // ✅ allow the player to click a cell
                } else {
                    errorMessage = "❌ Incorrect. Turn lost."
                    val nextTurn = if (game.turn == 1) 2 else 1
                    gameRef.child("turn").setValue(nextTurn)
                    currentWord = null
                    showQuestion = true
                }
                userAnswer = ""
            }) {
                Text("Check")
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Game board
        game.board.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    val color = when (cell) {
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
                                enabled = cell == 0 && winner == 0 && isPlayerTurn && !showQuestion
                            ) {
                                makeMove(
                                    gameRef,
                                    rowIndex,
                                    colIndex,
                                    game,
                                    uid
                                )
                                currentWord = null
                                showQuestion = true
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val newGame = Game(
                player1 = game.player1,
                player2 = game.player2
            )
            gameRef.setValue(newGame)
            currentWord = null
            showQuestion = true
            userAnswer = ""
            errorMessage = ""
        }) {
            Text("Restart")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = {
            navController.popBackStack()
        }) {
            Text("Exit")
        }
    }
}

// Handle the move
fun makeMove(
    ref: DatabaseReference,
    row: Int,
    column: Int,
    current: Game,
    uid: String?
) {
    if (uid == null) return
    val board = current.board.map { it.toMutableList() }.toMutableList()

    for (r in (board.size - 1) downTo 0) {
        if (board[r][column] == 0) {
            board[r][column] = current.turn
            break
        }
    }

    val nextTurn = if (current.turn == 1) 2 else 1

    val updatedGame = current.copy(
        board = board,
        turn = nextTurn
    )
    ref.setValue(updatedGame)
}

// Check if there is a winner
fun checkWinner(board: List<List<Int>>): Int {
    val rows = board.size
    val cols = board[0].size

    fun checkDirection(r: Int, c: Int, dr: Int, dc: Int): Int {
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
            checkDirection(r, c, 0, 1).takeIf { it != 0 }?.let { return it } // horizontal
            checkDirection(r, c, 1, 0).takeIf { it != 0 }?.let { return it } // vertical
            checkDirection(r, c, 1, 1).takeIf { it != 0 }?.let { return it } // diagonal down
            checkDirection(r, c, -1, 1).takeIf { it != 0 }?.let { return it } // diagonal up
        }
    }

    return 0
}

// Check if it's a draw
fun isDraw(board: List<List<Int>>): Boolean {
    return board.all { row -> row.none { it == 0 } }
}