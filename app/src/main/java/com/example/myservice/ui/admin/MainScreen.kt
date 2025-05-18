package com.example.myservice.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myservice.ui.common.InvoiceCollectionScreen
import com.example.myservice.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    parentNavController: NavHostController // For root navigation
) {
    val childNavController = rememberNavController()
    val navBackStackEntry by childNavController.currentBackStackEntryAsState()
    // Bottom bar visibility state
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val bottomBarHeight = 80.dp // Adjust based on your actual bottom bar height

    // Convert dp to pixels for animation
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.toPx() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Get current destination from child navigation
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = remember(currentRoute) {
        when (currentRoute) {
            Screen.AdminHome.route          -> "All Invoices"
            Screen.CollectionScreen.route   -> "Collection"
            Screen.EditInvoice.route.substringBefore("/{") -> "Edit Invoice"
            Screen.InvoiceDetails.route.substringBefore("/{") -> "Invoice Details"
            else                            -> "Billing App"
        }
    }
// Set layout direction to RTL for the drawer
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet {
                        DrawerContent(
                            onProfile = {
                                parentNavController.navigate(Screen.Profile.route)
                                scope.launch { drawerState.close() }
                            },
                            onSettings = {
                                parentNavController.navigate(Screen.Settings.route)
                                scope.launch { drawerState.close() }
                            },
                            onLogout = {
                                scope.launch { drawerState.close() }
                                onLogout()
                            }
                        )
                    }
                }
            }
        ) {
            // Set layout direction back to LTR for main content
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(currentTitle) },
                            actions = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = isBottomBarVisible,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            BottomAppBar(
                                modifier = Modifier.height(bottomBarHeight),
                                actions = {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.List, "Invoices") },
                                        label = { Text("Invoices") },
                                        selected = currentRoute == Screen.AdminHome.route,
                                        onClick = {
                                            childNavController.navigate(Screen.AdminHome.route) {
                                                popUpTo(childNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )

                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Assessment, "Collection") },
                                        label = { Text("Collection") },
                                        selected = currentRoute == Screen.CollectionScreen.route,
                                        onClick = {
                                            childNavController.navigate(Screen.CollectionScreen.route) {
                                                popUpTo(childNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    },
                    content = { innerPadding ->
        NavHost(
            navController = childNavController,
            startDestination = Screen.AdminHome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.AdminHome.route) {
                AdminHomeScreen(
                    onInvoiceClick = { invoiceId ->
                        parentNavController.navigate(Screen.InvoiceDetails.createRoute(invoiceId.toString()))
                    },
                    onCreateInvoice = { parentNavController.navigate(Screen.CreateInvoice.route) },
                    onLogout = onLogout,
                    onScroll = { visible -> isBottomBarVisible = visible },
                    bottomBarHeight = bottomBarHeight
                )
            }
            composable(Screen.CollectionScreen.route) {
                InvoiceCollectionScreen(
                    onBack = { childNavController.popBackStack() }
                )
            }
        }
        }
        )
    }
}
}
}

@Composable
fun DrawerContent(
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        Divider()

        // Drawer Items
        NavigationDrawerItem(
            label = { Text("Profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            selected = false,
            onClick = onProfile
        )
        NavigationDrawerItem(
            label = { Text("Settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            selected = false,
            onClick = onSettings
        )

        Spacer(Modifier.weight(1f))

        // Logout
        Divider()
        NavigationDrawerItem(
            label = { Text("Logout") },
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout") },
            selected = false,
            onClick = onLogout
        )
    }
}
