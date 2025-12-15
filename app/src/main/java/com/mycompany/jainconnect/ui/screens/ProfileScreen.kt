package com.mycompany.jainconnect.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.jainconnect.JainViewModel
import com.mycompany.jainconnect.SessionManager
import com.mycompany.jainconnect.User
import com.mycompany.jainconnect.ui.theme.SaffronPrimary
import com.mycompany.jainconnect.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: JainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEditProfile: (User) -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = true)
    var showLogoutDialog by remember { mutableStateOf(false) }

    val isDark = ThemeState.isDarkMode
    val bgColor by animateColorAsState(
        if (isDark) Color(0xFF121212) else Color(0xFFFAFAFA), tween(400), label = "bg"
    )
    val cardColor by animateColorAsState(
        if (isDark) Color(0xFF1E1E1E) else Color.White, tween(400), label = "card"
    )
    val textColor by animateColorAsState(
        if (isDark) Color.White else Color.Black, tween(400), label = "text"
    )

    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)
        if (token != null) viewModel.fetchUserProfile(token)
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White,
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(28.dp))
                }
            },
            title = { Text("Logout", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = textColor, textAlign = TextAlign.Center) },
            text = { Text("Are you sure you want to logout?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        SessionManager(context).clearSession()
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Yes, Logout") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }, shape = RoundedCornerShape(12.dp)) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(color = if (isDark) Color(0xFF1E1E1E) else Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                    Text("My Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = textColor, modifier = Modifier.weight(1f))
                    IconButton(onClick = { userProfile?.let { onEditProfile(it) } }) {
                        Icon(Icons.Default.Edit, "Edit", tint = SaffronPrimary)
                    }
                }
            }
        },
        containerColor = bgColor
    ) { padding ->
        when {
            isLoading || userProfile == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SaffronPrimary)
                }
            }
            else -> {
                val user = userProfile!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))
                    
                    // Avatar
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(if (isDark) SaffronPrimary.copy(0.2f) else SaffronPrimary.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (user.name?.firstOrNull() ?: 'U').uppercase(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text(user.name ?: "Devotee", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = textColor)
                    Surface(shape = RoundedCornerShape(20.dp), color = if (isDark) SaffronPrimary.copy(0.2f) else SaffronPrimary.copy(0.1f)) {
                        Text("Member", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = SaffronPrimary)
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Personal Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SaffronPrimary)
                            ProfileInfoRow(Icons.Default.Email, "Email", user.email, isDark)
                            ProfileInfoRow(Icons.Default.Phone, "Phone", user.phone ?: "Not Provided", isDark)
                            ProfileInfoRow(Icons.Default.LocationOn, "Location", user.location ?: "Not Provided", isDark)
                            if (!user.gender.isNullOrEmpty()) {
                                ProfileInfoRow(Icons.Default.Person, "Gender", user.gender!!, isDark)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Logout Button
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String, isDark: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (isDark) SaffronPrimary.copy(0.1f) else SaffronPrimary.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = SaffronPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = if (isDark) Color.White else Color.Black)
        }
    }
}
