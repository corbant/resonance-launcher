package io.github.corbant.resonancelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import io.github.corbant.resonancelauncher.ui.LauncherViewModel
import io.github.corbant.resonancelauncher.ui.components.AppGrid
import io.github.corbant.resonancelauncher.ui.theme.ResonanceLauncherTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResonanceLauncherTheme {
                val apps by viewModel.appsState.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    AppGrid(apps = apps, onAppClick = { app -> viewModel.launchApp(app) })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.loadApps()
    }
}