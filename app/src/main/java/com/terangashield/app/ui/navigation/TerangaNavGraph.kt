package com.terangashield.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.ui.calls.CallDetailScreen
import com.terangashield.app.ui.calls.CallsScreen
import com.terangashield.app.ui.contacts.ContactsScreen
import com.terangashield.app.ui.dialer.DialerScreen
import com.terangashield.app.ui.home.HomeScreen
import com.terangashield.app.ui.messages.MessageDetailScreen
import com.terangashield.app.ui.messages.MessagesScreen
import com.terangashield.app.ui.messages.NewMessageScreen
import com.terangashield.app.ui.onboarding.ConsentScreen
import com.terangashield.app.ui.onboarding.CountryLanguageScreen
import com.terangashield.app.ui.onboarding.PermissionsRequestScreen
import com.terangashield.app.ui.onboarding.PrivacyScreen
import com.terangashield.app.ui.onboarding.TrustedContactSetupScreen
import com.terangashield.app.ui.onboarding.WelcomeScreen
import com.terangashield.app.ui.onboarding.WhyDefaultRolesScreen
import com.terangashield.app.ui.settings.ReportedNumbersScreen
import com.terangashield.app.ui.settings.SettingsScreen

@Composable
fun TerangaNavGraph(locator: ServiceLocator) {
    val navController = rememberNavController()
    val onboardingComplete by locator.userPreferencesRepository.onboardingComplete
        .collectAsStateWithLifecycle(initialValue = null)

    val startDestinationKnown = onboardingComplete != null
    if (!startDestinationKnown) return // court instant de chargement des préférences, pas d'écran de démarrage nécessaire

    val startDestination = if (onboardingComplete == true) Destinations.HOME else Destinations.ONBOARDING_WELCOME

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Destinations.bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TerangaBottomBar(currentRoute = currentRoute, navController = navController)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = androidx.compose.ui.Modifier.padding(padding),
        ) {
            composable(Destinations.ONBOARDING_WELCOME) {
                WelcomeScreen(onNext = { navController.navigate(Destinations.ONBOARDING_COUNTRY_LANGUAGE) })
            }
            composable(Destinations.ONBOARDING_COUNTRY_LANGUAGE) {
                CountryLanguageScreen(
                    locator = locator,
                    onNext = { navController.navigate(Destinations.ONBOARDING_WHY_DEFAULT_ROLES) },
                )
            }
            composable(Destinations.ONBOARDING_WHY_DEFAULT_ROLES) {
                WhyDefaultRolesScreen(onNext = { navController.navigate(Destinations.ONBOARDING_PRIVACY) })
            }
            composable(Destinations.ONBOARDING_PRIVACY) {
                PrivacyScreen(onNext = { navController.navigate(Destinations.ONBOARDING_TRUSTED_CONTACT) })
            }
            composable(Destinations.ONBOARDING_TRUSTED_CONTACT) {
                TrustedContactSetupScreen(
                    locator = locator,
                    onNext = { navController.navigate(Destinations.ONBOARDING_CONSENT) },
                )
            }
            composable(Destinations.ONBOARDING_CONSENT) {
                ConsentScreen(
                    locator = locator,
                    onNext = { navController.navigate(Destinations.ONBOARDING_PERMISSIONS) },
                )
            }
            composable(Destinations.ONBOARDING_PERMISSIONS) {
                PermissionsRequestScreen(
                    locator = locator,
                    onFinish = {
                        navController.navigate(Destinations.HOME) {
                            popUpTo(0)
                        }
                    },
                )
            }

            composable(Destinations.HOME) {
                HomeScreen(
                    locator = locator,
                    onOpenCall = { navController.navigate(Destinations.callDetail(it)) },
                    onOpenMessage = { navController.navigate(Destinations.messageDetail(it)) },
                )
            }
            composable(Destinations.CALLS) {
                CallsScreen(
                    locator = locator,
                    onOpenCall = { navController.navigate(Destinations.callDetail(it)) },
                    onOpenDialer = { navController.navigate(Destinations.DIALER) },
                    onOpenContacts = { navController.navigate(Destinations.CONTACTS) },
                )
            }
            composable(Destinations.DIALER) {
                DialerScreen(initialNumber = "", onBack = { navController.popBackStack() })
            }
            composable(Destinations.CONTACTS) {
                ContactsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Destinations.CALL_DETAIL,
                arguments = listOf(navArgument("callId") { type = androidx.navigation.NavType.LongType }),
            ) { backStack ->
                val callId = backStack.arguments?.getLong("callId") ?: return@composable
                CallDetailScreen(locator = locator, callId = callId, onBack = { navController.popBackStack() })
            }
            composable(Destinations.MESSAGES) {
                MessagesScreen(
                    locator = locator,
                    onOpenMessage = { navController.navigate(Destinations.messageDetail(it)) },
                    onNewMessage = { navController.navigate(Destinations.NEW_MESSAGE) },
                )
            }
            composable(Destinations.NEW_MESSAGE) {
                NewMessageScreen(
                    locator = locator,
                    initialRecipient = "",
                    onSent = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Destinations.MESSAGE_DETAIL,
                arguments = listOf(navArgument("messageId") { type = androidx.navigation.NavType.LongType }),
            ) { backStack ->
                val messageId = backStack.arguments?.getLong("messageId") ?: return@composable
                MessageDetailScreen(locator = locator, messageId = messageId, onBack = { navController.popBackStack() })
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    locator = locator,
                    onResetOnboarding = {
                        navController.navigate(Destinations.ONBOARDING_WELCOME) { popUpTo(0) }
                    },
                    onOpenReportHistory = { navController.navigate(Destinations.REPORTED_NUMBERS) },
                )
            }
            composable(Destinations.REPORTED_NUMBERS) {
                ReportedNumbersScreen(locator = locator, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun TerangaBottomBar(currentRoute: String?, navController: androidx.navigation.NavHostController) {
    data class Tab(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val labelRes: Int)

    val tabs = listOf(
        Tab(Destinations.HOME, Icons.Filled.Home, R.string.nav_home),
        Tab(Destinations.CALLS, Icons.Filled.Call, R.string.calls_title),
        Tab(Destinations.MESSAGES, Icons.Filled.Sms, R.string.messages_title),
        Tab(Destinations.SETTINGS, Icons.Filled.Settings, R.string.settings_title),
    )

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
            )
        }
    }
}
