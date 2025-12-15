package com.mycompany.jainconnect.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.components.LoadingDashboardShimner
import com.mycompany.jainconnect.ui.theme.SaffronPrimary
import com.mycompany.jainconnect.ui.theme.ThemeState
import kotlin.random.Random

// Daily Suvichar (Quotes)
val suvicharList = listOf(
    "जीवों पर दया करो, यही सबसे बड़ा धर्म है।" to "Acharya Hemchandra",
    "अहिंसा परमो धर्मः - अहिंसा सबसे बड़ा धर्म है।" to "Jain Sutra",
    "क्षमा वीरस्य भूषणम् - क्षमा वीरों का आभूषण है।" to "Mahavir Swami",
    "सत्य बोलो, सत्य का आचरण करो।" to "Tirthankar Updesh",
    "आत्मा ही परमात्मा है।" to "Acharya Kundkund",
    "जो दूसरों को दुःख नहीं देता, वही सच्चा साधु है।" to "Jain Philosophy"
)

// Jain Principles
data class JainPrinciple(
    val name: String,
    val meaning: String,
    val icon: ImageVector,
    val color: Color
)

val jainPrinciples = listOf(
    JainPrinciple("अहिंसा", "Non-Violence", Icons.Default.Favorite, Color(0xFFE91E63)),
    JainPrinciple("सत्य", "Truthfulness", Icons.Default.Verified, Color(0xFF4CAF50)),
    JainPrinciple("अस्तेय", "Non-Stealing", Icons.Default.Security, Color(0xFF2196F3)),
    JainPrinciple("ब्रह्मचर्य", "Celibacy", Icons.Default.SelfImprovement, Color(0xFF9C27B0)),
    JainPrinciple("अपरिग्रह", "Non-Possessiveness", Icons.Default.VolunteerActivism, Color(0xFFFF9800))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String?,
    todayTithi: String?,
    sunrise: String?,
    sunset: String?,
    isLoading: Boolean,
    onNavigate: (String) -> Unit
) {
    val isDark = ThemeState.isDarkMode
    
    // Get random suvichar for today
    val todaySuvichar = remember { suvicharList[Random.nextInt(suvicharList.size)] }
    
    // Animated colors
    val backgroundColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF121212) else Color.White,
        animationSpec = tween(500), label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isDark) Color.White else Color.Black,
        animationSpec = tween(500), label = "textColor"
    )
    val subtextColor by animateColorAsState(
        targetValue = if (isDark) Color.Gray else Color.Gray,
        animationSpec = tween(500), label = "subtextColor"
    )
    val cardColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1E1E1E) else Color.White,
        animationSpec = tween(500), label = "cardColor"
    )
    
    if (isLoading) {
        LoadingDashboardShimner()
    } else {
        Scaffold(containerColor = backgroundColor) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                // ========== HEADER ==========
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Jai Jinendra! 🙏", style = MaterialTheme.typography.bodyMedium, color = subtextColor)
                            Text(
                                text = userName ?: "Guest",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeToggleButton(isDarkMode = isDark, onToggle = { ThemeState.toggleTheme() })
                            Surface(onClick = { onNavigate("profile") }, shape = CircleShape, color = SaffronPrimary.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text((userName?.firstOrNull() ?: 'U').uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaffronPrimary)
                                }
                            }
                        }
                    }
                }

                // ========== DAILY SUVICHAR (SPIRITUAL QUOTE) ==========
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = if (isDark) listOf(Color(0xFF1A237E), Color(0xFF311B92))
                                        else listOf(SaffronPrimary.copy(0.9f), Color(0xFFFF7043))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FormatQuote, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("आज का सुविचार", color = Color.White.copy(0.9f), style = MaterialTheme.typography.labelLarge)
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    todaySuvichar.first,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic),
                                    color = Color.White
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("— ${todaySuvichar.second}", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.7f))
                            }
                        }
                    }
                }

                // ========== NAVKAR MANTRA CARD ==========
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { /* Open mantra screen */ },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFFFF8E1))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                                    .background(SaffronPrimary.copy(0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🙏", fontSize = 28.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("णमोकार मंत्र", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                                Text("Navkar Mantra - The Supreme Prayer", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                            }
                            Icon(Icons.Default.PlayCircle, null, tint = SaffronPrimary, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                // ========== JAIN PRINCIPLES (PANCH MAHAVRAT) ==========
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("पंच महाव्रत", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                        Text("The Five Great Vows", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        Spacer(Modifier.height(12.dp))
                        
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(jainPrinciples) { principle ->
                                Card(
                                    modifier = Modifier.width(100.dp).height(120.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) principle.color.copy(0.2f) else principle.color.copy(0.1f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(principle.icon, null, tint = principle.color, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text(principle.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = principle.color, textAlign = TextAlign.Center)
                                        Text(principle.meaning, style = MaterialTheme.typography.labelSmall, color = principle.color.copy(0.7f), textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }

                // ========== TODAY'S INFO ==========
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Today's Info", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                        Spacer(Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            InfoCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.CalendarMonth,
                                label = "Tithi", value = todayTithi ?: "Loading...",
                                cardColor = if (isDark) Color(0xFF1B3D2F) else Color(0xFFE8F5E9), iconColor = Color(0xFF43A047)
                            )
                            InfoCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.WbSunny,
                                label = "Sunrise", value = sunrise ?: "--:--",
                                cardColor = if (isDark) Color(0xFF3D3520) else Color(0xFFFFF8E1), iconColor = Color(0xFFFFA000)
                            )
                            InfoCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.NightsStay,
                                label = "Sunset", value = sunset ?: "--:--",
                                cardColor = if (isDark) Color(0xFF1A237E).copy(0.3f) else Color(0xFFE8EAF6), iconColor = Color(0xFF5C6BC0)
                            )
                        }
                    }
                }

                // ========== QUICK SPIRITUAL ACTIONS ==========
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Quick Actions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                        Spacer(Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionCard(
                                modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.MenuBook, text = "Sutras",
                                color = Color(0xFF673AB7), isDark = isDark
                            )
                            QuickActionCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.Notifications, text = "Reminders",
                                color = Color(0xFFE91E63), isDark = isDark
                            )
                            QuickActionCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.LocationOn, text = "Temples",
                                color = Color(0xFF009688), isDark = isDark
                            )
                            QuickActionCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.Restaurant, text = "Jain Food",
                                color = Color(0xFFFF5722), isDark = isDark
                            )
                        }
                    }
                }

                // ========== EXPLORE SECTION ==========
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Explore", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                        Spacer(Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FeatureCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.CalendarToday,
                                title = "Tithi", subtitle = "Jain Calendar",
                                cardColor = if (isDark) Color(0xFF0D47A1).copy(0.3f) else Color(0xFFE3F2FD),
                                iconColor = Color(0xFF1E88E5), onClick = { onNavigate("tithi") }
                            )
                            FeatureCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.Event,
                                title = "Events", subtitle = "Community",
                                cardColor = if (isDark) Color(0xFF880E4F).copy(0.3f) else Color(0xFFFCE4EC),
                                iconColor = Color(0xFFE91E63), onClick = { onNavigate("events") }
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FeatureCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.TempleHindu,
                                title = "Monks", subtitle = "Find Maharaj",
                                cardColor = if (isDark) Color(0xFF4A148C).copy(0.3f) else Color(0xFFF3E5F5),
                                iconColor = Color(0xFF9C27B0), onClick = { onNavigate("monks") }
                            )
                            FeatureCard(
                                modifier = Modifier.weight(1f), icon = Icons.Default.WbTwilight,
                                title = "Horizons", subtitle = "Sun Timings",
                                cardColor = if (isDark) Color(0xFFE65100).copy(0.3f) else Color(0xFFFFF3E0),
                                iconColor = Color(0xFFFF9800), onClick = { onNavigate("horizons") }
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FeatureCard(
                                modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.ContactSupport,
                                title = "Contact", subtitle = "Get Help",
                                cardColor = if (isDark) Color(0xFF004D40).copy(0.3f) else Color(0xFFE0F2F1),
                                iconColor = Color(0xFF00897B), onClick = { onNavigate("contact") }
                            )
                            Box(modifier = Modifier.weight(1f))
                        }
                        
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ========== QUICK ACTION CARD ==========
@Composable
fun QuickActionCard(modifier: Modifier, icon: ImageVector, text: String, color: Color, isDark: Boolean) {
    Card(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) color.copy(0.2f) else color.copy(0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = color, textAlign = TextAlign.Center)
        }
    }
}

// ========== ANIMATED THEME TOGGLE ==========
@Composable
fun ThemeToggleButton(isDarkMode: Boolean, onToggle: () -> Unit) {
    val rotation by animateFloatAsState(targetValue = if (isDarkMode) 180f else 0f, animationSpec = tween(500), label = "rotation")
    val bgColor by animateColorAsState(targetValue = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFFFF3E0), animationSpec = tween(500), label = "toggleBg")
    val iconColor by animateColorAsState(targetValue = if (isDarkMode) Color(0xFFFFD54F) else Color(0xFFFF9800), animationSpec = tween(500), label = "iconColor")
    
    Surface(onClick = onToggle, shape = CircleShape, color = bgColor, modifier = Modifier.size(44.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.rotate(rotation)) {
            Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, "Toggle Theme", tint = iconColor, modifier = Modifier.size(24.dp))
        }
    }
}

// ========== INFO CARD ==========
@Composable
fun InfoCard(modifier: Modifier, icon: ImageVector, label: String, value: String, cardColor: Color, iconColor: Color) {
    Card(
        modifier = modifier.height(100.dp), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = iconColor.copy(0.7f))
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = iconColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
    }
}

// ========== FEATURE CARD ==========
@Composable
fun FeatureCard(modifier: Modifier, icon: ImageVector, title: String, subtitle: String, cardColor: Color, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(120.dp).clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, title, tint = iconColor, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = iconColor)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = iconColor.copy(0.7f))
        }
    }
}
