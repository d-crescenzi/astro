package com.astro.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.astro.app.presentation.home.HomeScreen
import com.astro.app.presentation.test_flow.PreflightScreen
import com.astro.app.presentation.test_flow.PvtScreen
import com.astro.app.presentation.test_flow.ResultScreen
import com.astro.app.presentation.test_flow.TestFlowViewModel
import com.astro.app.pvt.PvtMode
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    permissions: Array<String>,
    sensorPermission: String,
    hasPermission: () -> Boolean,
    onPermissionResult: (Boolean) -> Unit,
) {
    val nav = rememberSwipeDismissableNavController()
    // Activity-scoped: shared by the screens of the test flow.
    val testVm: TestFlowViewModel = koinViewModel()

    AppScaffold(timeText = {}) {
        SwipeDismissableNavHost(navController = nav, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(
                    permissions = permissions,
                    sensorPermission = sensorPermission,
                    hasPermission = hasPermission,
                    onPermissionResult = onPermissionResult,
                    onEnterPreflight = {
                        testVm.begin(PvtMode.DEMO)
                        nav.navigate(Routes.PREFLIGHT) { launchSingleTop = true }
                    },
                    onEnterTest = {
                        testVm.begin(PvtMode.DEMO)
                        nav.navigate(Routes.TEST) { launchSingleTop = true }
                    },
                )
            }
            composable(Routes.PREFLIGHT) {
                PreflightScreen(
                    vm = testVm,
                    onStartTest = {
                        nav.navigate(Routes.TEST) {
                            popUpTo(Routes.PREFLIGHT) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.TEST) {
                PvtScreen(
                    testVm,
                    onFinished = {
                        nav.navigate(Routes.RESULT) {
                            popUpTo(Routes.HOME)
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.RESULT) {
                ResultScreen(vm = testVm, onDone = { nav.popBackStack(Routes.HOME, inclusive = false) })
            }
        }
    }
}
