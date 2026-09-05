package io.github.corbant.resonancelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import io.github.corbant.resonancelauncher.ui.LauncherViewModel
import io.github.corbant.resonancelauncher.ui.components.AppList
import io.github.corbant.resonancelauncher.ui.theme.ResonanceLauncherTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()
    private val navTabs = listOf("Tab1", "Tab2", "Tab3")
    private var currentTab = mutableIntStateOf(0)

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResonanceLauncherTheme {
                val apps by viewModel.appsState.collectAsState()
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    shape = RectangleShape,
                    colors = SurfaceDefaults.colors(
                        containerColor = Color.Black,
                        contentColor = Color.LightGray,
                    )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = currentTab.intValue,
                            modifier = Modifier
                                .focusRestorer()
                                .padding(top = 27.dp, start = 48.dp)
                                .clip(CircleShape),
                            containerColor = Color.DarkGray,
                        ) {
                            navTabs.forEachIndexed { index, tab ->
                                key(index) {
                                    Tab(
                                        selected = index == currentTab.intValue,
                                        onFocus = { currentTab.intValue = index }) {
                                        Text(
                                            text = tab,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 6.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        AppList(
                            apps = apps,
                            title = "Your Apps",
                            onAppClick = { app -> viewModel.launchApp(app) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.loadApps()
    }
}