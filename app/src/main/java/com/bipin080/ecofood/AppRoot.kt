package com.bipin080.ecofood

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.bipin080.ecofood.auth.LoginScreen
import com.bipin080.ecofood.auth.SignUpScreen
import com.bipin080.ecofood.auth.AuthViewModel
import com.bipin080.ecofood.ui.theme.*
import com.bipin080.ecofood.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

// --- Start of Definitions ---

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Plan : Screen("plan", "Plan", { Icon(Icons.Default.CalendarMonth, contentDescription = null) })
    object Pantry : Screen("pantry", "Pantry", { Icon(Icons.Default.Kitchen, contentDescription = null) })
    object RecipeGenerator : Screen("recipe_generator", "AI Recipes", { Icon(Icons.Default.AutoAwesome, contentDescription = null) })
    object LeftoverMagic : Screen("leftover_magic", "Leftover Magic", { Icon(Icons.Default.AutoFixHigh, contentDescription = null) })
    object Marketplace : Screen("marketplace", "Marketplace", { Icon(Icons.Default.Storefront, contentDescription = null) })
    object Profile : Screen("profile", "Profile", { Icon(Icons.Default.Person, contentDescription = null) })
}

val items = listOf(
    Screen.Plan,
    Screen.Pantry,
    Screen.RecipeGenerator,
    Screen.LeftoverMagic,
    Screen.Marketplace,
    Screen.Profile
)

object Graph {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
    const val RECIPE = "recipe_graph"
}

// --- End of Definitions ---

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    // ... (Auth logic remains the same)

    ScaffoldWithConditionalBottomBar(navController) {
        NavHost(navController = navController, startDestination = Graph.AUTH, modifier = Modifier.padding(it)) {
            navigation(startDestination = "login", route = Graph.AUTH) {
                composable("login") {
                    LoginScreen(
                        viewModel = viewModel(),
                        onLoginSuccess = { navController.navigate(Graph.MAIN) { popUpTo(Graph.AUTH) { inclusive = true } } },
                        onNavigateToSignUp = { navController.navigate("signup") }
                    )
                }
                composable("signup") {
                    SignUpScreen(
                        viewModel = viewModel(),
                        onSignUpSuccess = { navController.navigate(Graph.MAIN) { popUpTo(Graph.AUTH) { inclusive = true } } },
                        onNavigateToLogin = { navController.popBackStack() }
                    )
                }
            }

            navigation(startDestination = Screen.Plan.route, route = Graph.MAIN) {
                composable(Screen.Plan.route) { PlanScreen() }
                composable(Screen.Pantry.route) { PantryScreen() }
                composable(Screen.RecipeGenerator.route) {
                    val recipeViewModel: RecipeViewModel = viewModel(navController.getBackStackEntry(Graph.MAIN))
                    CookScreen(
                        onGenerateRecipe = { recipe ->
                            recipeViewModel.setRecipe(recipe)
                            navController.navigate(Graph.RECIPE)
                        }
                    )
                }
                composable(Screen.LeftoverMagic.route) { LeftoverMagicScreen() }
                composable(Screen.Marketplace.route) {
                    MarketplaceScreen(onAddItem = { navController.navigate("add_marketplace_item") })
                }
                composable(Screen.Profile.route) { ProfileScreen() }
                composable("add_marketplace_item") {
                    AddMarketplaceItemScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
            
            navigation(startDestination = "recipe_details", route = Graph.RECIPE) {
                 composable("recipe_details") {
                    val recipeViewModel: RecipeViewModel = viewModel(navController.getBackStackEntry(Graph.MAIN))
                    RecipeScreen(
                        recipeViewModel = recipeViewModel,
                        onNavigateUp = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}


@Composable
fun ScaffoldWithConditionalBottomBar(navController: NavHostController, content: @Composable (PaddingValues) -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.hierarchy?.none { it.route == Graph.AUTH } == true && currentDestination?.parent?.route != Graph.RECIPE

     Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
         content(it)
     }
}
