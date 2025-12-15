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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.jainconnect.JainViewModel
import com.mycompany.jainconnect.ui.theme.SaffronPrimary
import com.mycompany.jainconnect.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    viewModel: JainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val addEventResult by viewModel.addEventResult.observeAsState()
    
    LaunchedEffect(addEventResult) {
        addEventResult?.let { result ->
            isLoading = false
            if (result.startsWith("Success")) {
                Toast.makeText(context, "Event added!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            }
        }
    }

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

    Scaffold(
        topBar = {
            Surface(color = if (isDark) Color(0xFF1E1E1E) else Color.White, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                    Text("Add Event", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = textColor)
                }
            }
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        leadingIcon = { Icon(Icons.Default.Event, null, tint = SaffronPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
                    )
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        leadingIcon = { Icon(Icons.Default.Description, null, tint = SaffronPrimary) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = SaffronPrimary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary),
                            placeholder = { Text("DD/MM/YYYY") }
                        )
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time") },
                            leadingIcon = { Icon(Icons.Default.Schedule, null, tint = SaffronPrimary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary),
                            placeholder = { Text("HH:MM") }
                        )
                    }
                    
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = SaffronPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter event title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("jwt_token", "") ?: ""
                    viewModel.submitNewEvent(token, title, city, date, time, description)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Event", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
