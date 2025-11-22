package com.bipin080.ecofood

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.bipin080.ecofood.auth.AuthViewModel
import com.bipin080.ecofood.auth.LoginScreen
import com.bipin080.ecofood.auth.SignUpScreen
import com.bipin080.ecofood.ui.theme.*
import com.bipin080.ecofood.viewmodel.MarketplaceViewModel
import com.bipin080.ecofood.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Plan : Screen("plan", "Plan", { Icon(Icons.Default.CalendarMonth, null) })
    object Pantry : Screen("pantry", "Pantry", { Icon(Icons.Default.Kitchen, null) })
    object RecipeGenerator : Screen("recipe_generator", "AI Recipes", { Icon(Icons.Default.AutoAwesome, null) })
    object MyRecipes : Screen("my_recipes", "My Recipes", { Icon(Icons.Default.MenuBook, null) })
    object LeftoverMagic : Screen("leftover_magic", "Leftover Magic", { Icon(Icons.Default.AutoFixHigh, null) })
    object Marketplace : Screen("marketplace", "Marketplace", { Icon(Icons.Default.Storefront, null) })
    object Profile : Screen("profile", "Profile", { Icon(Icons.Default.Person, null) })
}

val bottomNavItems = listOf(
    Screen.Plan, Screen.Pantry, Screen.RecipeGenerator,
    Screen.LeftoverMagic, Screen.Marketplace, Screen.Profile
)

object Graph {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
    const val RECIPE = "recipe_graph"
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = if (FirebaseAuth.getInstance().currentUser != null)
            Graph.MAIN else Graph.AUTH
    }

    val showBottomBar = currentDestination?.hierarchy?.any { it.route == Graph.MAIN } == true

    if (startDestination != null) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { screen ->
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
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination!!,
                modifier = Modifier.padding(innerPadding)
            ) {

                // -------------------- AUTH GRAPH --------------------
                navigation(startDestination = "login", route = Graph.AUTH) {

                    composable("login") {
                        val authViewModel: AuthViewModel = viewModel()
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate(Graph.MAIN) {
                                    popUpTo(Graph.AUTH) { inclusive = true }
                                }
                            },
                            onNavigateToSignUp = { navController.navigate("signup") }
                        )
                    }

                    composable("signup") {
                        val authViewModel: AuthViewModel = viewModel()
                        SignUpScreen(
                            viewModel = authViewModel,
                            onSignUpSuccess = {
                                navController.navigate(Graph.MAIN) {
                                    popUpTo(Graph.AUTH) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = { navController.popBackStack() }
                        )
                    }
                }

                // -------------------- MAIN GRAPH --------------------
                navigation(startDestination = Screen.Plan.route, route = Graph.MAIN) {

                    composable(Screen.Plan.route) {
                        PlanScreen(onMyRecipesClick = {
                            navController.navigate(Screen.MyRecipes.route)
                        })
                    }

                    composable(Screen.Pantry.route) { PantryScreen() }

                    composable(Screen.RecipeGenerator.route) {
                        val mainEntry = remember { navController.getBackStackEntry(Graph.MAIN) }
                        val viewModel: RecipeViewModel = viewModel(mainEntry)

                        CookScreen(
                            onGenerateRecipe = { recipe ->
                                viewModel.setRecipe(recipe)
                                navController.navigate(Graph.RECIPE)
                            }
                        )
                    }


                    composable(Screen.MyRecipes.route) {
                        val mainEntry = remember { navController.getBackStackEntry(Graph.MAIN) }
                        val recipeViewModel: RecipeViewModel = viewModel(mainEntry)

                        val recipes by recipeViewModel.savedRecipes.collectAsState(initial = emptyList())

                        MyRecipesScreen(
                            recipes = recipes,
                            onOpenRecipe = {
                                recipeViewModel.setRecipe(it)
                                navController.navigate(route = Graph.RECIPE)
                            },
                            onDeleteRecipe = {
                                recipeViewModel.deleteRecipe(recipe = it)
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )

                    }

                    composable(Screen.LeftoverMagic.route) { LeftoverMagicScreen() }

                    composable(Screen.Marketplace.route) {
                        val mainEntry = remember { navController.getBackStackEntry(Graph.MAIN) }
                        val viewModel: MarketplaceViewModel = viewModel(mainEntry)
                        MarketplaceScreen(
                            marketplaceViewModel = viewModel,
                            onAddItem = { navController.navigate("add_marketplace_item") },
                            onViewMyListings = { navController.navigate("my_listings") }
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(Graph.AUTH) {
                                    popUpTo(Graph.MAIN) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("add_marketplace_item") {
                        val mainEntry = navController.getBackStackEntry(Graph.MAIN)
                        val vm: MarketplaceViewModel = viewModel(mainEntry)
                        AddMarketplaceItemScreen(
                            marketplaceViewModel = vm,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("my_listings") {
                        val mainEntry = navController.getBackStackEntry(Graph.MAIN)
                        val vm: MarketplaceViewModel = viewModel(mainEntry)
                        MyListingsScreen(
                            marketplaceViewModel = vm,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                // -------------------- RECIPE DETAILS GRAPH --------------------
                navigation(startDestination = "recipe_details", route = Graph.RECIPE) {
                    composable("recipe_details") {
                        val mainEntry = navController.getBackStackEntry(Graph.MAIN)
                        val vm: RecipeViewModel = viewModel(mainEntry)
                        RecipeScreen(
                            recipeViewModel = vm,
                            onNavigateUp = { navController.popBackStack() }
                        )

                    }
                }
            }
        }
    }
}
