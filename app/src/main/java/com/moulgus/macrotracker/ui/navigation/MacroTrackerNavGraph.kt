package com.moulgus.macrotracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moulgus.macrotracker.ui.screens.addmeal.AddMealScreen
import com.moulgus.macrotracker.ui.screens.addproduct.AddProductScreen
import com.moulgus.macrotracker.ui.screens.products.ProductsScreen
import com.moulgus.macrotracker.ui.screens.settings.SettingsScreen
import com.moulgus.macrotracker.ui.screens.statistics.StatisticsScreen
import com.moulgus.macrotracker.ui.screens.today.TodayScreen

private object Routes {
    const val TODAY = "today"
    const val ADD_MEAL = "add_meal"
    const val PRODUCTS = "products"
    const val ADD_PRODUCT = "add_product"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"
}

@Composable
fun MacroTrackerNavGraph() {
    val navController = rememberNavController()

    fun returnToToday() {
        navController.navigate(Routes.TODAY) {
            popUpTo(Routes.TODAY) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun returnToProducts() {
        navController.navigate(Routes.PRODUCTS) {
            popUpTo(Routes.PRODUCTS) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.TODAY
    ) {
        composable(Routes.TODAY) {
            TodayScreen(
                onAddMealClick = {
                    navController.navigate(Routes.ADD_MEAL)
                },
                onProductsClick = {
                    navController.navigate(Routes.PRODUCTS)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onStatisticsClick = {
                    navController.navigate(Routes.STATISTICS)
                }
            )
        }

        composable(Routes.ADD_MEAL) {
            AddMealScreen(
                onBackClick = {
                    returnToToday()
                }
            )
        }

        composable(Routes.PRODUCTS) {
            ProductsScreen(
                onBackClick = {
                    returnToToday()
                },
                onAddProductClick = {
                    navController.navigate(Routes.ADD_PRODUCT)
                }
            )
        }

        composable(Routes.ADD_PRODUCT) {
            AddProductScreen(
                onBackClick = {
                    returnToProducts()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    returnToToday()
                }
            )
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onBackClick = {
                    returnToToday()
                }
            )
        }
    }
}