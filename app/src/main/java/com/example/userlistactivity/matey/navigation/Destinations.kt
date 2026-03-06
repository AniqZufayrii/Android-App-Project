package com.example.userlistactivity.matey.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector?) {
    object Login : Screen("login", "Login", Icons.Default.Lock)
    object SignUp : Screen("signup", "Sign Up", Icons.Default.PersonAdd)
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Bills : Screen("bills", "Bills", Icons.Default.Receipt)
    object Chores : Screen("chores", "Chores", Icons.Default.CheckCircle)
    object Friends : Screen("friends", "Friends", Icons.Default.People)
    object SplitBill : Screen("split_bill", "Split", Icons.Default.Add)
    object BillDetail : Screen("bill_detail/{billId}", "Detail", Icons.Default.Info) {
        fun createRoute(billId: Int) = "bill_detail/$billId"
    }
    object AIChat : Screen("aichat", "Chat", Icons.Default.SmartToy)
}