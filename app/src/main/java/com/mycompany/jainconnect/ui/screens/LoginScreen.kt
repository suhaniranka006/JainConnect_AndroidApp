package com.mycompany.jainconnect.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.jainconnect.JainViewModel
import com.mycompany.jainconnect.ui.theme.SaffronPrimary
import com.mycompany.jainconnect.ui.theme.ThemeState
import com.mycompany.jainconnect.utils.NetworkResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: JainViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    
    val loginResult by viewModel.loginResult.observeAsState()
    
    LaunchedEffect(loginResult) {
        when (loginResult) {
            is NetworkResult.Loading -> isLoading = true
            is NetworkResult.Success -> {
                isLoading = false
                val token = (loginResult as NetworkResult.Success).data?.token
                if (token != null) {
                    context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                        .edit().putString("jwt_token", token).apply()
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
            }
            is NetworkResult.Error -> {
                isLoading = false
                Toast.makeText(context, (loginResult as NetworkResult.Error).message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
            null -> {}
        }
    }

    val isDark = ThemeState.isDarkMode
    val bgColor by animateColorAsState(
        if (isDark) Color(0xFF121212) else Color.White, tween(400), label = "bg"
    )
    val cardColor by animateColorAsState(
        if (isDark) Color(0xFF1E1E1E) else Color(0xFFFAFAFA), tween(400), label = "card"
    )
    val textColor by animateColorAsState(
        if (isDark) Color.White else Color.Black, tween(400), label = "text"
    )

    Scaffold(containerColor = bgColor) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            
            // Logo/Title
            Text("🙏", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Jain Connect",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = SaffronPrimary
            )
            Text("Welcome back!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(Modifier.height(40.dp))
            
            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Login", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = textColor)
                    Spacer(Modifier.height(20.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = SaffronPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.height(14.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = SaffronPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.performLogin(email, password)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Login", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = Color.Gray)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Register", color = SaffronPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
