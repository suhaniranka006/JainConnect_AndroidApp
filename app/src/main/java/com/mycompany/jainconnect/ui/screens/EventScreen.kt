package com.mycompany.jainconnect.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.jainconnect.Event
import com.mycompany.jainconnect.JainViewModel
import com.mycompany.jainconnect.ui.theme.SaffronPrimary
import com.mycompany.jainconnect.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    viewModel: JainViewModel = hiltViewModel(),
    onAddEventClick: () -> Unit
) {
    val eventList by viewModel.eventList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = true)
    var searchQuery by remember { mutableStateOf("") }

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

    LaunchedEffect(Unit) { viewModel.fetchEvents() }
    
    // Filter locally based on search
    val filteredList = remember(eventList, searchQuery) {
        if (searchQuery.isBlank()) eventList
        else eventList.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.location.contains(searchQuery, ignoreCase = true) ||
            it.date.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            Surface(color = if (isDark) Color(0xFF1E1E1E) else Color.White, shadowElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "Events",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search events...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = if (isDark) Color(0xFF3C3C3C) else Color(0xFFE0E0E0),
                            focusedContainerColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5),
                            unfocusedContainerColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
                        ),
                        singleLine = true
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEventClick,
                containerColor = SaffronPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Event")
            }
        },
        containerColor = bgColor
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SaffronPrimary)
                }
            }
            filteredList.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(if (searchQuery.isNotEmpty()) "No results found" else "No events yet", color = textColor)
                        if (searchQuery.isEmpty()) {
                            Text("Add your first event!", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { event ->
                        EventCard(event, cardColor, textColor, isDark)
                    }
                    item { Spacer(Modifier.height(72.dp)) } // Space for FAB
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, cardColor: Color, textColor: Color, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF1E88E5).copy(0.2f) else Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Event, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text(event.location ?: "TBD", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            
            if (!event.description.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color.LightGray else Color.DarkGray,
                    maxLines = 2
                )
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF2C2C2C) else Color(0xFFEEEEEE))
            Spacer(Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = SaffronPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text(event.date, style = MaterialTheme.typography.labelMedium, color = textColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = SaffronPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text(event.time ?: "Time TBD", style = MaterialTheme.typography.labelMedium, color = textColor)
                }
            }
        }
    }
}
