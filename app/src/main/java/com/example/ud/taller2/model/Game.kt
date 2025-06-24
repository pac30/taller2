package com.example.ud.taller2.model

data class Game(
    val board: List<List<Int>> = List(6) { List(7) { 0 } },
    val turn: Int = 1,
    val player1: String = "",
    val player2: String = ""
)