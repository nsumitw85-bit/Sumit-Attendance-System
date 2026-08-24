package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CustomThemeConfig
import com.example.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val currentTheme by viewModel.appTheme.collectAsState()
    val currentCustomConfig by viewModel.customThemeConfig.collectAsState()

    var primaryColor by remember(currentCustomConfig) { mutableStateOf(currentCustomConfig.primaryColor) }
    var backgroundColor by remember(currentCustomConfig) { mutableStateOf(currentCustomConfig.backgroundColor) }
    var buttonColor by remember(currentCustomConfig) { mutableStateOf(currentCustomConfig.buttonColor) }
    var textColor by remember(currentCustomConfig) { mutableStateOf(currentCustomConfig.textColor) }
    var cardColor by remember(currentCustomConfig) { mutableStateOf(currentCustomConfig.cardColor) }

    val presetColors = listOf(
        0xFF1565C0L, // Deep Blue
        0xFF2E7D32L, // Dark Green
        0xFF6A1B9AL, // Purple
        0xFFC62828L, // Dark Red
        0xFFE65100L, // Orange
        0xFF37474FL, // Blue Grey
        0xFF00838FL, // Cyan Dark
        0xFF4E342EL  // Brown
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("थीम सेटिंग्ज (Theme Settings)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "मागे")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Mode Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "थीम मोड निवडा (Select Theme Mode)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeOptionItem(
                        title = "Light Mode (लाईट)",
                        selected = currentTheme == "light",
                        onClick = { viewModel.setTheme("light") }
                    )

                    ThemeOptionItem(
                        title = "Dark Mode (डार्क)",
                        selected = currentTheme == "dark",
                        onClick = { viewModel.setTheme("dark") }
                    )

                    ThemeOptionItem(
                        title = "Custom Theme (सानुकूल रंग)",
                        selected = currentTheme == "custom",
                        onClick = { viewModel.setTheme("custom") }
                    )
                }
            }

            // Custom Theme Palette Editor
            if (currentTheme == "custom") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "सानुकूल रंग निवडा (Customize Colors)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        ColorSelectorRow(
                            label = "प्राथमिक रंग (Primary Color):",
                            selectedColor = primaryColor,
                            options = presetColors,
                            onSelect = { primaryColor = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ColorSelectorRow(
                            label = "बटण रंग (Button Color):",
                            selectedColor = buttonColor,
                            options = presetColors,
                            onSelect = { buttonColor = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetCustomTheme()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("रीसेट करा")
                            }

                            Button(
                                onClick = {
                                    val newConfig = CustomThemeConfig(
                                        primaryColor = primaryColor,
                                        backgroundColor = backgroundColor,
                                        buttonColor = buttonColor,
                                        textColor = textColor,
                                        cardColor = cardColor
                                    )
                                    viewModel.saveCustomTheme(newConfig)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("जतन करा")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 15.sp)
    }
}

@Composable
fun ColorSelectorRow(
    label: String,
    selectedColor: Long,
    options: List<Long>,
    onSelect: (Long) -> Unit
) {
    Column {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.take(6).forEach { colorVal ->
                val isSelected = selectedColor == colorVal
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(colorVal))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { onSelect(colorVal) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
