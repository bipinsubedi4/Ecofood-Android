package com.bipin080.ecofood

import android.net.http.SslCertificate.restoreState
import android.net.http.SslCertificate.saveState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bipin080.ecofood.ui.theme.CookScreen
import com.bipin080.ecofood.ui.theme.PantryScreen
import com.bipin080.ecofood.ui.theme.PlanScreen
import com.bipin080.ecofood.ui.theme.ProfileScreen
import com.bipin080.ecofood.ui.theme.ShareScreen


sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Pantry : Tab("pantry", "Pantry", Icons.Outlined.Home)
    data object Cook : Tab("cook", "Cook", Icons.Outlined.MenuBook)
    data object Share : Tab("share", "Share", Icons.Outlined.Share)
    data object Plan : Tab("plan", "Plan", Icons.Outlined.CalendarMonth)
    data object Profile : Tab("profile", "Profile", Icons.Outlined.Person)
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val tabs = listOf(Tab.Pantry, Tab.Cook, Tab.Share, Tab.Plan, Tab.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by nav.currentBackStackEntryAsState()
                val current = backStack?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = Tab.Pantry.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Tab.Pantry.route) { PantryScreen() }
            composable(Tab.Cook.route) { CookScreen() }
            composable(Tab.Share.route) { ShareScreen() }
            composable(Tab.Plan.route) { PlanScreen() }
            composable(Tab.Profile.route) { ProfileScreen() }
        }
    }
}

@Composable
fun SimpleScreen(label: String) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
