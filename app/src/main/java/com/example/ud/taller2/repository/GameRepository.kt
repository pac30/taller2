package com.example.ud.taller2.repository

import com.example.ud.taller2.model.Palabra
import com.example.ud.taller2.model.Partida
import com.google.firebase.database.DatabaseReference

class GameRepository(private val db: DatabaseReference) {

    // Listen for changes in the game
    fun escucharPartida(codigo: String, onUpdate: (Partida) -> Unit) {
        db.child("partidas").child(codigo).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val partida = snapshot.getValue(Partida::class.java)
                if (partida != null) {
                    onUpdate(partida)
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                // You can handle errors here if needed
            }
        })
    }

    fun removerEscucha(codigo: String) {
        db.child("partidas").child(codigo).removeEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {}
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    fun hacerMovimiento(codigo: String, fila: Int, columna: Int, partida: Partida) {
        val tablero = partida.tablero.map { it.toMutableList() }.toMutableList()
        for (row in (tablero.size - 1) downTo 0) {
            if (tablero[row][columna] == 0) {
                tablero[row][columna] = partida.turno
                break
            }
        }

        val nuevoTurno = if (partida.turno == 1) 2 else 1
        val nuevaPartida = partida.copy(tablero = tablero, turno = nuevoTurno)
        db.child("partidas").child(codigo).setValue(nuevaPartida)
    }

    fun cambiarTurno(codigo: String, nuevoTurno: Int) {
        db.child("partidas").child(codigo).child("turno").setValue(nuevoTurno)
    }

    fun verificarGanador(tablero: List<List<Int>>): Int {
        val rows = tablero.size
        val cols = tablero[0].size

        fun checkDirection(r: Int, c: Int, dr: Int, dc: Int): Int {
            val player = tablero[r][c]
            if (player == 0) return 0
            for (i in 1..3) {
                val nr = r + dr * i
                val nc = c + dc * i
                if (nr !in 0 until rows || nc !in 0 until cols || tablero[nr][nc] != player) {
                    return 0
                }
            }
            return player
        }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val directions = listOf(
                    checkDirection(r, c, 0, 1),
                    checkDirection(r, c, 1, 0),
                    checkDirection(r, c, 1, 1),
                    checkDirection(r, c, -1, 1)
                )
                directions.firstOrNull { it != 0 }?.let { return it }
            }
        }

        return 0
    }

    fun verificarEmpate(tablero: List<List<Int>>): Boolean {
        return tablero.all { row -> row.all { it != 0 } }
    }

    fun obtenerPalabras(onResult: (List<Palabra>) -> Unit) {
        db.child("palabras").get().addOnSuccessListener { snapshot ->
            val lista = snapshot.children.mapNotNull { it.getValue(Palabra::class.java) }
            onResult(lista)
        }
    }

    // ✅ FUNCTION TO GENERATE UNIQUE CODES WITH LETTERS AND NUMBERS
    fun generarCodigoUnico(
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        fun intentarGenerar() {
            val chars = ('A'..'Z') + ('0'..'9')
            val nuevoCodigo = (1..6)
                .map { chars.random() }
                .joinToString("")

            db.child("partidas").child(nuevoCodigo).get().addOnSuccessListener {
                if (!it.exists()) {
                    onSuccess(nuevoCodigo)
                } else {
                    intentarGenerar() // Try again if it already exists
                }
            }.addOnFailureListener {
                onError()
            }
        }

        intentarGenerar()
    }
}