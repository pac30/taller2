package com.example.ud.taller2.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ud.taller2.ui.theme.Taller2Theme
import com.example.ud.taller2.viewmodel.GameViewModel
import com.example.ud.taller2.viewmodel.GameViewModelFactory
import com.example.ud.taller2.repository.GameRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            Taller2Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(auth = auth, navController = navController)
                    }
                    composable("lobby") {
                        LobbyScreen(navController = navController)
                    }
                    composable("juego/{codigo}") { backStackEntry ->
                        val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
                        val repository = GameRepository(FirebaseDatabase.getInstance().reference)
                        val gameViewModel: GameViewModel = viewModel(
                            factory = GameViewModelFactory(repository, codigo)
                        )
                        GameScreen(navController = navController, viewModel = gameViewModel)
                    }
                }
            }
        }
    }
}
