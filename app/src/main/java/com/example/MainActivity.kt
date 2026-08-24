package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MonthlyHistoryScreen
import com.example.ui.screens.SalaryCalculationScreen
import com.example.ui.screens.SettingsAndReportsScreen
import com.example.ui.screens.ThemeSettingsScreen
import com.example.ui.screens.WorkerManagementScreen
import com.example.ui.theme.SumitAttendanceTheme
import com.example.viewmodel.AttendanceViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: AttendanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = applicationContext as Application
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        )[AttendanceViewModel::class.java]

        setContent {
            val themeSetting by viewModel.appTheme.collectAsState()
            val customThemeConfig by viewModel.customThemeConfig.collectAsState()

            SumitAttendanceTheme(
                themeMode = themeSetting,
                customConfig = customThemeConfig
            ) {
                SumitAttendanceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SumitAttendanceApp(viewModel: AttendanceViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val startDestination = if (isLoggedIn) "home" else "login"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAttendance = { navController.navigate("attendance") },
                    onNavigateToMonthlySummary = { navController.navigate("monthly_history") },
                    onNavigateToSalaryCalculation = { navController.navigate("salary_calculation") },
                    onNavigateToDailyPdf = { navController.navigate("settings") },
                    onNavigateToWorkers = { navController.navigate("workers") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("attendance") {
                AttendanceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("salary_calculation") {
                SalaryCalculationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("monthly_history") {
                MonthlyHistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToDateAttendance = { _ ->
                        navController.navigate("attendance")
                    }
                )
            }

            composable("workers") {
                WorkerManagementScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsAndReportsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToMonthlySummary = { navController.navigate("monthly_history") },
                    onNavigateToSalaryCalculation = { navController.navigate("salary_calculation") },
                    onNavigateToTheme = { navController.navigate("theme_settings") },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("theme_settings") {
                ThemeSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
