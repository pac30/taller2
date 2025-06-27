package com.example.ud.taller2.logic

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

fun generarCodigo(): String {
    val chars = ('A'..'Z')
    return (1..6)
        .map { chars.random() }
        .joinToString("")
}