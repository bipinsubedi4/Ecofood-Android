package com.bipin080.ecofood

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.bipin080.ecofood.auth.AuthViewModel
import com.bipin080.ecofood.auth.LoginScreen
import com.bipin080.ecofood.auth.SignUpScreen
import com.bipin080.ecofood.ui.theme.AddMarketplaceItemScreen
import com.bipin080.ecofood.ui.theme.CookScreen
import com.bipin080.ecofood.ui.theme.LeftoverMagicScreen
import com.bipin080.ecofood.ui.theme.MarketplaceScreen
import com.bipin080.ecofood.ui.theme.MyListingsScreen
import com.bipin080.ecofood.ui.theme.MyRecipesScreen
import com.bipin080.ecofood.ui.theme.PantryScreen
import com.bipin080.ecofood.ui.theme.PlanScreen
import com.bipin080.ecofood.ui.theme.ProfileScreen
import com.bipin080.ecofood.ui.theme.RecipeScreen
import com.bipin080.ecofood.viewmodel.MarketplaceViewModel
import com.bipin080.ecofood.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

// ---------- Navigation definitions ----------

sealed class Screen(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
) {
    object Plan : Screen("plan", "Plan", {
        androidx.compose.material3.Icon(Icons.Default.CalendarMonth, contentDescription = null)
    })

    object Pantry : Screen("pantry", "Pantry", {
        androidx.compose.material3.Icon(Icons.Default.Kitchen, contentDescription = null)
    })

    object RecipeGenerator : Screen("recipe_generator", "AI Recipes", {
        androidx.compose.material3.Icon(Icons.Default.AutoAwesome, contentDescription = null)
    })

    object MyRecipes : Screen("my_recipes", "My Recipes", {
        androidx.compose.material3.Icon(Icons.Default.MenuBook, contentDescription = null)
    })

    object LeftoverMagic : Screen("leftover_magic", "Leftover Magic", {
        androidx.compose.material3.Icon(Icons.Default.AutoFixHigh, contentDescription = null)
    })

    object Marketplace : Screen("marketplace", "Marketplace", {
        androidx.compose.material3.Icon(Icons.Default.Storefront, contentDescription = null)
    })

    object Profile : Screen("profile", "Profile", {
        androidx.compose.material3.Icon(Icons.Default.Person, contentDescription = null)
    })
}

// Bottom bar items (MyRecipes removed from here – it’s on the dashboard instead)
val bottomNavItems = listOf(
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

// ---------- Root composable ----------

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
            Graph.MAIN
        } else {
            Graph.AUTH
        }
    }

    // show bottom bar only on MAIN graph
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
                                selected = currentDestination
                                    ?.hierarchy
                                    ?.any { it.route == screen.route } == true,
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
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination!!,
                modifier = Modifier.padding(innerPadding)
            ) {

                // -------- AUTH GRAPH --------
                navigation(
                    startDestination = "login",
                    route = Graph.AUTH
                ) {
                    composable("login") {
                        val authViewModel: AuthViewModel = viewModel()
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate(Graph.MAIN) {
                                    popUpTo(Graph.AUTH) { inclusive = true }
                                }
                            },
                            onNavigateToSignUp = {
                                navController.navigate("signup")
                            }
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

                // -------- MAIN GRAPH --------
                navigation(
                    startDestination = Screen.Plan.route,
                    route = Graph.MAIN
                ) {
                    // PLAN (dashboard) – now has My Recipes shortcut card
                    composable(Screen.Plan.route) {
                        PlanScreen(
                            onMyRecipesClick = {
                                navController.navigate(Screen.MyRecipes.route)
                            }
                        )
                    }

                    composable(Screen.Pantry.route) {
                        PantryScreen()
                    }

                    composable(Screen.RecipeGenerator.route) {
                        val mainGraphEntry =
                            remember { navController.getBackStackEntry(Graph.MAIN) }
                        val recipeViewModel: RecipeViewModel = viewModel(mainGraphEntry)

                        CookScreen(
                            onGenerateRecipe = { recipe ->
                                recipeViewModel.setRecipe(recipe)
                                navController.navigate(Graph.RECIPE)
                            }
                        )
                    }

                    composable(Screen.MyRecipes.route) {
                        val mainGraphEntry =
                            remember { navController.getBackStackEntry(Graph.MAIN) }
                        val recipeViewModel: RecipeViewModel = viewModel(mainGraphEntry)

                        val recipes by recipeViewModel.savedRecipes
                            .collectAsState(initial = emptyList())

                        MyRecipesScreen(
                            recipes = recipes,
                            onOpenRecipe = { recipe ->
                                recipeViewModel.setRecipe(recipe)
                                navController.navigate(Graph.RECIPE)
                            },
                            onDeleteRecipe = { recipe ->
                                recipeViewModel.deleteRecipe(recipe)
                            }
                        )
                    }

                    composable(Screen.LeftoverMagic.route) {
                        LeftoverMagicScreen()
                    }

                    composable(Screen.Marketplace.route) {
                        val mainGraphEntry =
                            remember { navController.getBackStackEntry(Graph.MAIN) }
                        val marketplaceViewModel: MarketplaceViewModel = viewModel(mainGraphEntry)
                        MarketplaceScreen(
                            marketplaceViewModel = marketplaceViewModel,
                            onAddItem = { navController.navigate("add_marketplace_item") },
                            onViewMyListings = { navController.navigate("my_listings") }
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen()
                    }

                    composable("add_marketplace_item") {
                        val mainGraphEntry =
                            remember { navController.getBackStackEntry(Graph.MAIN) }
                        val marketplaceViewModel: MarketplaceViewModel = viewModel(mainGraphEntry)
                        AddMarketplaceItemScreen(
                            marketplaceViewModel = marketplaceViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("my_listings") {
                        val mainGraphEntry =
                            remember { navController.getBackStackEntry(Graph.MAIN) }
                        val marketplaceViewModel: MarketplaceViewModel = viewModel(mainGraphEntry)
                        MyListingsScreen(
                            marketplaceViewModel = marketplaceViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                // -------- RECIPE DETAILS GRAPH --------
                navigation(
                    startDestination = "recipe_details",
                    route = Graph.RECIPE
                ) {
                    composable("recipe_details") {
                        val mainGraphEntry =
                            remember { navController.getBackStackEntry(Graph.MAIN) }
                        val recipeViewModel: RecipeViewModel = viewModel(mainGraphEntry)
                        RecipeScreen(
                            recipeViewModel = recipeViewModel,
                            onNavigateUp = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
