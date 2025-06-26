// GameViewModelFactory.kt
package com.example.ud.taller2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ud.taller2.repository.GameRepository

class GameViewModelFactory(
    private val repository: GameRepository,
    private val codigoPartida: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(repository, codigoPartida) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
