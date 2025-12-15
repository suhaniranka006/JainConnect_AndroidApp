package com.mycompany.jainconnect

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mycompany.jainconnect.ui.screens.*
import com.mycompany.jainconnect.utils.NetworkResult

@Composable
fun JainConnectRoot(viewModel: JainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // --- Global State Observations ---
    val loginResult by viewModel.loginResult.observeAsState()
    val signupResult by viewModel.signupResult.observeAsState()
    val userProfile by viewModel.userProfile.observeAsState()
    val tithiList by viewModel.filteredTithis.observeAsState(initial = emptyList())
    val horizonList by viewModel.horizonList.observeAsState(initial = emptyList())

    // Derived State for Home Screen
    val userName = userProfile?.name ?: "Guest"
    val todayTithi = tithiList.firstOrNull()?.name ?: "Loading Tithi..."
    val todaySun = horizonList.firstOrNull()
    val sunrise = todaySun?.sunrise ?: "--:--"
    val sunset = todaySun?.sunset ?: "--:--"

    // --- Side Effects for Navigation ---

    // Handle Login Success
    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is NetworkResult.Success -> {
                // Determine destination based on token? Or just go home
                // Ideally persist token here if Repository doesn't do it automatically
                // Although Repository usually handles it. Assuming ViewModel/Repo saves token.
                // If not, we might need shared prefs here.
                // Based on previous code, ViewModel calls repo.loginUser which returns Response.
                // NOTE: The previous login implementation in Activity saved to SharedPrefs.
                // The ViewModel logic I saw earlier might NOT be saving it to SharedPrefs
                // automatically.
                // I need to explicitly save it here if the Repo doesn't.
                // Let's assume we need to save it.
                val token = result.data?.token
                if (token != null) {
                    val sharedPref =
                            context.getSharedPreferences(
                                    "auth_prefs",
                                    android.content.Context.MODE_PRIVATE
                            )
                    with(sharedPref.edit()) {
                        putString("jwt_token", token)
                        apply()
                    }
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        popUpTo("splash") { inclusive = true }
                    }
                    // Fetch initial data after login
                    viewModel.fetchUserProfile(token)
                    viewModel.fetchTithis()
                    viewModel.fetchSunData()
                }
            }
            is NetworkResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(signupResult) {
        if (signupResult != null && signupResult!!.isSuccessful) {
            Toast.makeText(context, "Registration Successful! Please Login.", Toast.LENGTH_SHORT)
                    .show()
            navController.popBackStack() // Go back to Login
        } else if (signupResult != null) {
            Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                    onNavigateToNext = {
                        // Check if user is already logged in
                        val sharedPref =
                                context.getSharedPreferences(
                                        "auth_prefs",
                                        android.content.Context.MODE_PRIVATE
                                )
                        val token = sharedPref.getString("jwt_token", null)
                        if (token != null) {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                            // Pre-fetch data
                            viewModel.fetchUserProfile(token)
                            viewModel.fetchTithis()
                            viewModel.fetchSunData()
                        } else {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
            )
        }

        composable("login") {
            LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            // Trigger fetches if lists are empty and we have a token
            // (Optional, just safety net)
            HomeScreen(
                    userName = userName,
                    todayTithi = todayTithi,
                    sunrise = sunrise,
                    sunset = sunset,
                    isLoading = loginResult is NetworkResult.Loading, // Or separate loading state
                    onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("tithi") { TithiScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("events") {
            EventScreen(onAddEventClick = { navController.navigate("add_event") })
        }
        composable("monks") { MonksScreen(onAddMonkClick = { navController.navigate("add_monk") }) }
        composable("add_event") {
            AddEventScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("add_monk") { AddMonkScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("horizons") { HorizonsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("contact") { ContactScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("profile") {
            ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditProfile = { user ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("user", user)
                        navController.navigate("edit_profile")
                    },
                    onLogout = {
                        // Navigate to login and clear back stack
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
            )
        }
        composable("edit_profile") {
            val user =
                    navController.previousBackStackEntry?.savedStateHandle?.get<
                            com.mycompany.jainconnect.User>("user")
            // Fallback: If user is null (reloading), try userProfile from ViewModel
            val currentUser = user ?: userProfile

            if (currentUser != null) {
                EditProfileScreen(
                        user = currentUser,
                        onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Loading or Error state
                androidx.compose.material3.Text("Loading Profile...")
            }
        }
    }
}
