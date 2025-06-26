package com.example.ud.taller2

import com.example.ud.taller2.view.generarCodigo
import com.example.ud.taller2.view.verificarEmpate
import com.example.ud.taller2.view.verificarGanador
import org.junit.Assert.*
import org.junit.Test

class PruebasLogicaJuego {

    @Test
    fun pruebaVictoriaHorizontal() {
        val tablero = List(6) { MutableList(7) { 0 } }
        tablero[5][0] = 1
        tablero[5][1] = 1
        tablero[5][2] = 1
        tablero[5][3] = 1

        val ganador = verificarGanador(tablero)
        assertEquals(1, ganador)
    }

    @Test
    fun pruebaVictoriaVertical() {
        val tablero = List(6) { MutableList(7) { 0 } }
        tablero[2][3] = 2
        tablero[3][3] = 2
        tablero[4][3] = 2
        tablero[5][3] = 2

        val ganador = verificarGanador(tablero)
        assertEquals(2, ganador)
    }

    @Test
    fun pruebaEmpate() {
        val tablero = List(6) { MutableList(7) { 1 } }
        val esEmpate = verificarEmpate(tablero)
        assertTrue(esEmpate)
    }

    @Test
    fun pruebaLongitudCodigo() {
        val codigo = generarCodigo()
        assertEquals(6, codigo.length)
    }

    @Test
    fun pruebaCodigoMayusculas() {
        val codigo = generarCodigo()
        assertTrue(codigo.all { it in 'A'..'Z' })
    }
}