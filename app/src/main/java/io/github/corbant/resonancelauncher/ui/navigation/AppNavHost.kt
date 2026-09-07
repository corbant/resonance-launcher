package io.github.corbant.resonancelauncher.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.corbant.resonancelauncher.data.AppRepository
import io.github.corbant.resonancelauncher.data.server.SetupServerManager
import io.github.corbant.resonancelauncher.ui.features.home.HomeScreen
import io.github.corbant.resonancelauncher.ui.features.home.HomeViewModel
import io.github.corbant.resonancelauncher.ui.features.home.createHomeViewModelFactory
import io.github.corbant.resonancelauncher.ui.features.settings.SettingsScreen
import io.github.corbant.resonancelauncher.ui.features.settings.SettingsViewModel
import io.github.corbant.resonancelauncher.ui.features.settings.createSettingsViewModelFactory

@Composable
fun AppNavHost(
    navController: NavHostController,
    appRepository: AppRepository,
    serverManager: SetupServerManager,
    modifier: Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier
    ) {
        composable<Route.Home> {
            val homeViewModel: HomeViewModel = viewModel(
                factory = createHomeViewModelFactory(appRepository)
            )

            HomeScreen(
                onNavigateToSettings = { navController.navigate(Route.Settings) },
                onMediaClick = { id, type ->
                    navController.navigate(Route.MediaDetails(id, type))
                },
                viewModel = homeViewModel
            )
        }

        composable<Route.Settings> {
            val settingsViewModel: SettingsViewModel =
                viewModel(factory = createSettingsViewModelFactory(serverManager))
            SettingsScreen(viewModel = settingsViewModel)
        }

        composable<Route.MediaDetails> {
            // TODO: implement this
        }
    }

}