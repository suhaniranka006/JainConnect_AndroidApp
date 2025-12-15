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
import com.mycompany.jainconnect.HorizonItem
import com.mycompany.jainconnect.JainViewModel
import com.mycompany.jainconnect.ui.theme.SaffronPrimary
import com.mycompany.jainconnect.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonsScreen(
    viewModel: JainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val horizonList by viewModel.horizonList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = true)

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

    LaunchedEffect(Unit) { viewModel.fetchSunData() }

    Scaffold(
        topBar = {
            Surface(color = if (isDark) Color(0xFF1E1E1E) else Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                    Column {
                        Text(
                            "Horizons",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Text("Sunrise & Sunset Times", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        },
        containerColor = bgColor
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SaffronPrimary)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading sun data...", color = Color.Gray)
                    }
                }
            }
            horizonList.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌅", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("No data available", color = textColor)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(horizonList) { item ->
                        HorizonCard(item, cardColor, textColor, isDark)
                    }
                }
            }
        }
    }
}

@Composable
fun HorizonCard(item: HorizonItem, cardColor: Color, textColor: Color, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) SaffronPrimary.copy(0.2f) else Color(0xFFFFF3E0)
            ) {
                Text(
                    item.date,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SaffronPrimary
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Sunrise
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFFFFA000).copy(0.2f) else Color(0xFFFFF8E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WbSunny, null, tint = Color(0xFFFFA000), modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(item.sunrise, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                    Text("Sunrise", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(70.dp)
                        .background(if (isDark) Color(0xFF3C3C3C) else Color(0xFFEEEEEE))
                )
                
                // Sunset
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF5C6BC0).copy(0.2f) else Color(0xFFE8EAF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NightsStay, null, tint = Color(0xFF5C6BC0), modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(item.sunset, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                    Text("Sunset", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}
