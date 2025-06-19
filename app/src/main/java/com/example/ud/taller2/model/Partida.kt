package com.example.ud.taller2.model

data class Partida(
    val tablero: List<List<Int>> = List(6) { List(7) { 0 } },
    val turno: Int = 1,
    val jugador1: String = "",
    val jugador2: String = ""
)