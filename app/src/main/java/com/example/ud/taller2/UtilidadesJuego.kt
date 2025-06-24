package com.example.ud.taller2

// Función para verificar si hay un ganador en el tablero
fun verificarGanador(tablero: List<List<Int>>): Int {
    val filas = tablero.size
    val columnas = tablero[0].size

    fun verificarDireccion(f: Int, c: Int, df: Int, dc: Int): Int {
        val jugador = tablero[f][c]
        if (jugador == 0) return 0
        for (i in 1..3) {
            val nf = f + df * i
            val nc = c + dc * i
            if (nf !in 0 until filas || nc !in 0 until columnas || tablero[nf][nc] != jugador) return 0
        }
        return jugador
    }

    for (f in 0 until filas) {
        for (c in 0 until columnas) {
            verificarDireccion(f, c, 0, 1).takeIf { it != 0 }?.let { return it } // Horizontal
            verificarDireccion(f, c, 1, 0).takeIf { it != 0 }?.let { return it } // Vertical
            verificarDireccion(f, c, 1, 1).takeIf { it != 0 }?.let { return it } // Diagonal ↘
            verificarDireccion(f, c, -1, 1).takeIf { it != 0 }?.let { return it } // Diagonal ↗
        }
    }

    return 0 // No hay ganador
}

// Función para verificar si el tablero está lleno (empate)
fun verificarEmpate(tablero: List<List<Int>>): Boolean {
    return tablero.all { fila -> fila.none { it == 0 } }
}

// Función para generar un código aleatorio de 6 letras mayúsculas
fun generarCodigo(): String {
    val letras = ('A'..'Z')
    return (1..6).map { letras.random() }.joinToString("")
}