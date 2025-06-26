package com.example.ud.taller2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ud.taller2.model.Palabra
import com.example.ud.taller2.model.Partida
import com.example.ud.taller2.repository.GameRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
    private val codigoPartida: String
) : ViewModel() {

    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val _partida = MutableStateFlow(Partida())
    val partida: StateFlow<Partida> = _partida

    private val _palabraActual = MutableStateFlow<Palabra?>(null)
    val palabraActual: StateFlow<Palabra?> = _palabraActual

    private val _respuestaUsuario = MutableStateFlow("")
    val respuestaUsuario: StateFlow<String> = _respuestaUsuario

    private val _mensajeError = MutableStateFlow("")
    val mensajeError: StateFlow<String> = _mensajeError

    private val _mostrarPregunta = MutableStateFlow(true)
    val mostrarPregunta: StateFlow<Boolean> = _mostrarPregunta

    private val _winner = MutableStateFlow(0)
    val winner: StateFlow<Int> = _winner

    private val _showDraw = MutableStateFlow(false)
    val showDraw: StateFlow<Boolean> = _showDraw

    private val _isTurnoJugador = MutableStateFlow(false)
    val isTurnoJugador: StateFlow<Boolean> = _isTurnoJugador

    init {
        // ✅ Escucha actualizaciones de la partida
        repository.escucharPartida(codigoPartida) { nuevaPartida ->
            _partida.value = nuevaPartida
            _winner.value = repository.verificarGanador(nuevaPartida.tablero)
            _showDraw.value = repository.verificarEmpate(nuevaPartida.tablero) && _winner.value == 0
            _isTurnoJugador.value = esMiTurno(nuevaPartida)

            // ✅ Verifica si debe cargar palabra
            cargarPalabraSiEsTurno()
        }

        // ✅ Opción adicional: también reacciona al cambio de turno para cargar palabra
        viewModelScope.launch {
            isTurnoJugador.collect { turno ->
                if (turno) {
                    cargarPalabraSiEsTurno()
                }
            }
        }
    }

    private fun esMiTurno(p: Partida): Boolean {
        return (uid == p.jugador1 && p.turno == 1) || (uid == p.jugador2 && p.turno == 2)
    }

    private fun cargarPalabraSiEsTurno() {
        viewModelScope.launch {
            if (_isTurnoJugador.value && _palabraActual.value == null && _mostrarPregunta.value) {
                repository.obtenerPalabras { lista ->
                    if (lista.isNotEmpty()) {
                        _palabraActual.value = lista.random()
                    }
                }
            }
        }
    }

    fun onRespuestaChange(text: String) {
        _respuestaUsuario.value = text
    }

    fun verificarRespuesta() {
        val palabra = _palabraActual.value ?: return
        val respuesta = _respuestaUsuario.value.trim()

        if (respuesta.equals(palabra.eng, ignoreCase = true)) {
            _mensajeError.value = ""
            _mostrarPregunta.value = false
        } else {
            _mensajeError.value = "❌ Incorrecto. Turno perdido."
            val nuevoTurno = if (_partida.value.turno == 1) 2 else 1
            repository.cambiarTurno(codigoPartida, nuevoTurno)
            _palabraActual.value = null
            _mostrarPregunta.value = true
        }
        _respuestaUsuario.value = ""
    }

    fun hacerMovimiento(columna: Int) {
        val p = _partida.value
        repository.hacerMovimiento(codigoPartida, 0, columna, p) // fila se calcula internamente
        _palabraActual.value = null
        _mostrarPregunta.value = true
    }

    override fun onCleared() {
        super.onCleared()
        repository.removerEscucha(codigoPartida)
    }

    fun generarCodigoUnico(
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference

        fun intentarGenerar() {
            val chars = ('A'..'Z') + ('0'..'9') // Lista combinada correctamente
            val nuevoCodigo = (1..6) // 6 letras/cifras
                .map { chars.random() }
                .joinToString("")

            db.child("partidas").child(nuevoCodigo).get().addOnSuccessListener {
                if (!it.exists()) {
                    onSuccess(nuevoCodigo)
                } else {
                    intentarGenerar()
                }
            }.addOnFailureListener {
                onError()
            }
        }

        intentarGenerar()
    }

}
