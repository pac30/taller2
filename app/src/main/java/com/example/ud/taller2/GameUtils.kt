package com.example.ud.taller2

fun checkWinner(board: List<List<Int>>): Int {
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

fun checkDraw(board: List<List<Int>>): Boolean {
    return board.all { row -> row.none { it == 0 } }
}

fun generateCode(): String {
    val chars = ('A'..'Z')
    return (1..6).map { chars.random() }.joinToString("")
}