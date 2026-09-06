package io.github.corbant.resonancelauncher.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.corbant.resonancelauncher.data.AppRepository
import io.github.corbant.resonancelauncher.ui.features.home.HomeScreen
import io.github.corbant.resonancelauncher.ui.features.home.HomeViewModel
import io.github.corbant.resonancelauncher.ui.features.home.createHomeViewModelFactory

@Composable
fun AppNavHost(
    navController: NavHostController,
    appRepository: AppRepository,
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
                onNavigateToSetup = { navController.navigate(Route.Setup) },
                onMediaClick = { id, type ->
                    navController.navigate(Route.MediaDetails(id, type))
                },
                viewModel = homeViewModel
            )
        }

        composable<Route.Setup> {
            // TODO: implement this
        }

        composable<Route.MediaDetails> {
            // TODO: implement this
        }
    }

}