package com.example.userlistactivity.matey.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Debt(
    val person: String,
    val amount: Double,
    val isOwedToYou: Boolean // True if they owe you, false if you owe them
)

enum class BillCategory(val icon: ImageVector, val color: Color) {
    UTILITIES(Icons.Default.FlashOn, Color(0xFF4CAF50)),
    INTERNET(Icons.Default.Info, Color(0xFF2196F3)),
    GROCERIES(Icons.Default.ShoppingCart, Color(0xFFFF9800)),
    RENT(Icons.Default.Home, Color(0xFF9C27B0)),
    OTHER(Icons.Default.Star, Color(0xFF607D8B))
}

enum class ChorePriority { HIGH, MEDIUM, LOW }