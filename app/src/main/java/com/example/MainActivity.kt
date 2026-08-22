package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppNavTab
import com.example.ui.StudyViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyAITheme(darkTheme = true) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            ) {
                AppNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setNavTab(tab) },
                        icon = {
                            when (tab) {
                                AppNavTab.DASHBOARD -> Icon(
                                    imageVector = if (isSelected) Icons.Default.Dashboard else Icons.Default.DashboardCustomize,
                                    contentDescription = "Dashboard"
                                )
                                AppNavTab.AI_SCHEDULER -> Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Planner"
                                )
                                AppNavTab.POMODORO -> Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Focus Timer"
                                )
                                AppNavTab.RECALL_SUITE -> Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "AI Recall"
                                )
                                AppNavTab.ANALYTICS -> Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Analytics"
                                )
                            }
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LavenderOnPrimary,
                            selectedTextColor = LavenderPrimary,
                            indicatorColor = MauveOnContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentTab,
            animationSpec = tween(250),
            modifier = Modifier.padding(innerPadding),
            label = "tab_crossfade"
        ) { tab ->
            when (tab) {
                AppNavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppNavTab.AI_SCHEDULER -> AIScheduleBuilderScreen(viewModel = viewModel)
                AppNavTab.POMODORO -> PomodoroTimerScreen(viewModel = viewModel)
                AppNavTab.RECALL_SUITE -> ActiveRecallSuiteScreen(viewModel = viewModel)
                AppNavTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
            }
        }
    }
}
