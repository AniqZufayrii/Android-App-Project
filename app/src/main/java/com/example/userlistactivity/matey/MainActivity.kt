package com.example.userlistactivity.matey

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.userlistactivity.matey.navigation.Screen
import com.example.userlistactivity.matey.ui.* 
import com.example.userlistactivity.matey.ui.theme.AppDark
import com.example.userlistactivity.matey.ui.theme.AppOrange
import com.example.userlistactivity.matey.ui.theme.AppWhite

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels {
        AppViewModelFactory((application as MateyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colorScheme = lightColorScheme(
                primary = AppDark,
                background = AppOrange,
                surface = AppWhite
            )

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    window.statusBarColor = AppOrange.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                }
            }

            MaterialTheme(colorScheme = colorScheme) {
                MainNavigationHost(viewModel)
            }
        }
    }
}

@Composable
fun MainNavigationHost(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showChoreDialog by remember { mutableStateOf(false) }
    var showFriendDialog by remember { mutableStateOf(false) }

    val bills by viewModel.allBills.collectAsState()
    val chores by viewModel.allChores.collectAsState()
    val friends by viewModel.allFriends.collectAsState()

    Scaffold(
        containerColor = AppOrange,
        bottomBar = {
            if (shouldShowNavigation(currentRoute)) {
                MyFloatingBottomBar(navController) {
                    when (currentRoute) {
                        Screen.Chores.route -> showChoreDialog = true
                        Screen.Friends.route -> showFriendDialog = true
                        else -> navController.navigate(Screen.SplitBill.route)
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(bills, friends, navController)
            }
            composable(Screen.Bills.route) {
                BillsScreen(bills, { viewModel.deleteBill(it) }, navController)
            }
            composable(
                route = Screen.BillDetail.route,
                arguments = listOf(navArgument("billId") { type = NavType.IntType })
            ) { backStackEntry ->
                val billId = backStackEntry.arguments?.getInt("billId")
                if (billId != null) {
                    BillDetailScreen(
                        billId = billId,
                        bills = bills,
                        onBack = { navController.popBackStack() },
                        onHome = { 
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        },
                        onSettle = { bill ->
                            viewModel.updateBill(bill.copy(isSettled = true))
                        },
                        onUpdate = { viewModel.updateBill(it) }
                    )
                }
            }

            composable(Screen.Chores.route) {
                ChoresScreen(chores, friends, { viewModel.updateChore(it) }, { viewModel.deleteChore(it) })
            }
            composable(Screen.Friends.route) {
                FriendsScreen(friends, bills, { viewModel.deleteFriend(it) })
            }
            composable(Screen.SplitBill.route) {
                SplitBillScreen(
                    friends = friends,
                    onCancel = { navController.popBackStack() },
                    onSave = { newBill ->
                        viewModel.insertBill(newBill)
                        navController.popBackStack()
                    }
                )
            }
        }

        if (showChoreDialog) {
            AddChoreDialog(friends = friends, onDismiss = { showChoreDialog = false }, onAdd = { viewModel.insertChore(it); showChoreDialog = false })
        }

        if (showFriendDialog) {
            AddFriendDialog(onDismiss = { showFriendDialog = false }, onAdd = { viewModel.insertFriend(it); showFriendDialog = false })
        }
    }
}

fun shouldShowNavigation(route: String?): Boolean {
    return route != Screen.SplitBill.route
}

fun shouldShowFab(route: String?): Boolean {
    return false // FAB is now integrated into the bottom bar
}
