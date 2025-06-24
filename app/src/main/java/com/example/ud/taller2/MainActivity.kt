package com.example.ud.taller2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ud.taller2.ui.theme.Taller2Theme
import com.google.firebase.auth.FirebaseAuth
import com.ud.taller2.GameScreen

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
                    composable("game/{code}") { backStackEntry ->
                        val code = backStackEntry.arguments?.getString("code") ?: ""
                        GameScreen(navController = navController, gameCode = code)
                    }
                }
            }
        }
    }
}