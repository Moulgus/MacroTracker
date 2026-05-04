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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.moulgus.macrotracker.ui.screens.productunits.ProductUnitsScreen


private object Routes {
    const val TODAY = "today"
    const val ADD_MEAL = "add_meal"
    const val PRODUCTS = "products"
    const val ADD_PRODUCT = "add_product"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"

    const val PRODUCT_UNITS_BASE = "product_units"
    const val PRODUCT_ID_ARG = "productID"
    const val PRODUCT_UNITS = "$PRODUCT_UNITS_BASE/{$PRODUCT_ID_ARG}"

    const val MEAL_ID_ARG = "mealID"
    const val EDIT_MEAL_BASE = "edit_meal"
    const val EDIT_MEAL = "$EDIT_MEAL_BASE/{$MEAL_ID_ARG}"

    fun editMealRoute(mealID: Long): String {
        return "$EDIT_MEAL_BASE/$mealID"
    }

    fun productUnitsRoute(productID: Long): String {
        return "$PRODUCT_UNITS_BASE/$productID"
    }
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
                },
                onEditMealClick = { mealID ->
                    navController.navigate(Routes.editMealRoute(mealID))
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
                },
                onProductUnitsClick = { productID ->
                    navController.navigate(Routes.productUnitsRoute(productID))
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

        composable(
            route = Routes.PRODUCT_UNITS,
            arguments = listOf(
                navArgument(Routes.PRODUCT_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val productID = backStackEntry.arguments?.getLong(Routes.PRODUCT_ID_ARG)

            if (productID != null) {
                ProductUnitsScreen(
                    productID = productID,
                    onBackClick = {
                        returnToProducts()
                    }
                )
            }
        }

        composable(
            route = Routes.EDIT_MEAL,
            arguments = listOf(
                navArgument(Routes.MEAL_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val mealID = backStackEntry.arguments?.getLong(Routes.MEAL_ID_ARG)

            if (mealID != null) {
                AddMealScreen(
                    editMealID = mealID,
                    onBackClick = {
                        returnToToday()
                    }
                )
            }
        }
    }
}