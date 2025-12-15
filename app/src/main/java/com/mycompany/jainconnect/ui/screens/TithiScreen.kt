package com.mycompany.jainconnect.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mycompany.jainconnect.JainViewModel
import com.mycompany.jainconnect.Tithi
import com.mycompany.jainconnect.ui.theme.SaffronPrimary
import com.mycompany.jainconnect.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TithiScreen(
    viewModel: JainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val tithiList by viewModel.filteredTithis.observeAsState(initial = emptyList())
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

    LaunchedEffect(Unit) { viewModel.fetchTithis() }
    
    // Filter locally based on search
    val filteredList = remember(tithiList, searchQuery) {
        if (searchQuery.isBlank()) tithiList
        else tithiList.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.date.contains(searchQuery, ignoreCase = true) 
        }
    }

    Scaffold(
        topBar = {
            Surface(color = if (isDark) Color(0xFF1E1E1E) else Color.White, shadowElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                        }
                        Text(
                            "Jain Tithi",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search tithi...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
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
                        Text("📅", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(if (searchQuery.isNotEmpty()) "No results found" else "No Tithi found", color = textColor)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { tithi ->
                        TithiCard(tithi, cardColor, textColor, isDark)
                    }
                }
            }
        }
    }
}

@Composable
fun TithiCard(tithi: Tithi, cardColor: Color, textColor: Color, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) SaffronPrimary.copy(0.2f) else Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = SaffronPrimary, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tithi.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Text(
                    tithi.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.Gray else Color.DarkGray
                )
                if (!tithi.details.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tithi.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
