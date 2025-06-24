package com.example.ud.taller2

import org.junit.Assert.*
import org.junit.Test

class GameLogicTest {

    @Test
    fun testHorizontalWin() {
        val board = List(6) { MutableList(7) { 0 } }
        board[5][0] = 1
        board[5][1] = 1
        board[5][2] = 1
        board[5][3] = 1

        val winner = checkWinner(board)
        assertEquals(1, winner)
    }

    @Test
    fun testVerticalWin() {
        val board = List(6) { MutableList(7) { 0 } }
        board[2][3] = 2
        board[3][3] = 2
        board[4][3] = 2
        board[5][3] = 2

        val winner = checkWinner(board)
        assertEquals(2, winner)
    }

    @Test
    fun testDrawBoard() {
        val board = List(6) { MutableList(7) { 1 } }
        val draw = checkDraw(board)
        assertTrue(draw)
    }

    @Test
    fun testGenerateCodeLength() {
        val code = generateCode()
        assertEquals(6, code.length)
    }

    @Test
    fun testGenerateCodeIsUppercase() {
        val code = generateCode()
        assertTrue(code.all { it in 'A'..'Z' })
    }
}