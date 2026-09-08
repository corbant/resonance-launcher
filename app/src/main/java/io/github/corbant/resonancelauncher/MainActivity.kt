package io.github.corbant.resonancelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.corbant.resonancelauncher.data.repository.AppRepository
import io.github.corbant.resonancelauncher.data.repository.LauncherPreferencesRepository
import io.github.corbant.resonancelauncher.data.server.SetupServerManager
import io.github.corbant.resonancelauncher.ui.navigation.AppNavHost
import io.github.corbant.resonancelauncher.ui.theme.ResonanceLauncherTheme

class MainActivity : ComponentActivity() {

    private val appRepository by lazy {
        AppRepository(applicationContext)
    }

    private val preferencesRepository by lazy {
        LauncherPreferencesRepository(applicationContext)
    }

    private val serverManager by lazy {
        SetupServerManager(applicationContext)
    }

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ResonanceLauncherTheme {
                val navController = rememberNavController()

                AppNavHost(
                    navController = navController,
                    appRepository = appRepository,
                    preferencesRepository = preferencesRepository,
                    serverManager = serverManager,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )

            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
}