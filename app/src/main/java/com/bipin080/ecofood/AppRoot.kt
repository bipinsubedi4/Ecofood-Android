package com.bipin080.ecofood

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bipin080.ecofood.ui.theme.CookScreen
import com.bipin080.ecofood.ui.theme.PantryScreen
import com.bipin080.ecofood.ui.theme.PlanScreen
import com.bipin080.ecofood.ui.theme.RecipeScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Plan : Screen("plan", "Plan", { Icon(Icons.Default.CalendarMonth, contentDescription = null) })
    object Pantry : Screen("pantry", "Pantry", { Icon(Icons.Default.Kitchen, contentDescription = null) })
    object Cook : Screen("cook", "Cook", { Icon(Icons.Default.Book, contentDescription = null) })
}

val items = listOf(
    Screen.Plan,
    Screen.Pantry,
    Screen.Cook,
)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Plan.route, Modifier.padding(innerPadding)) {
            composable(Screen.Plan.route) { PlanScreen() }
            composable(Screen.Pantry.route) { PantryScreen() }
            composable(Screen.Cook.route) { 
                CookScreen(onGenerateRecipe = { recipe ->
                    val encodedRecipe = URLEncoder.encode(recipe, StandardCharsets.UTF_8.toString())
                    navController.navigate("recipe/$encodedRecipe")
                })
            }
            composable(
                "recipe/{recipe}",
                arguments = listOf(navArgument("recipe") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipe = backStackEntry.arguments?.getString("recipe")
                if (recipe != null) {
                    RecipeScreen(recipe = recipe, onNavigateUp = { navController.popBackStack() })
                }
            }
        }
    }
}
