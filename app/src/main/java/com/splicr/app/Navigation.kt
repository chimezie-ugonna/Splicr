package com.splicr.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.splicr.app.ui.screens.GetStartedScreen
import com.splicr.app.ui.screens.OnboardingScreen
import com.splicr.app.utils.SharedPreferenceUtil

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = if (SharedPreferenceUtil.onboarded()) {
        "GetStartedScreen"
    } else {
        "OnboardingScreen"
    }
) {
    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(route = "OnboardingScreen") {
            OnboardingScreen(navController = navController)
        }
        composable(route = "GetStartedScreen") {
            BackHandler(enabled = true) {

            }
            GetStartedScreen(navController = navController)
        }/*composable(route = "PermissionsScreen") {
            BackHandler(enabled = true) {

            }
            PermissionsScreen(navController = navController)
        }
        composable(route = "StartScreen") {
            BackHandler(enabled = true) {

            }
            StartScreen(navController = navController)
        }
        composable(route = "GetStartedScreen") {
            GetStartedScreen(navController = navController)
        }
        composable(route = "LoginScreen") {
            LoginScreen(navController = navController)
        }
        composable(route = "ResetPasswordScreen") {
            ResetPasswordScreen(navController = navController)
        }
        composable(
            route = "ResetPasswordRequestSentScreen/{email}",
            arguments = listOf(navArgument("email") {
                type = NavType.StringType
            })
        ) {
            val email = it.arguments?.getString("email") ?: ""

            ResetPasswordRequestSentScreen(navController = navController, emailValue = email)
        }
        composable(route = "ProfilePictureUploadScreen") {
            BackHandler(enabled = true) {

            }
            ProfilePictureUploadScreen(navController = navController)
        }
        composable(route = "BottomNavigationScreen") {
            BackHandler(enabled = true) {

            }
            BottomNavigationScreen(navController = navController)
        }
        composable(route = "LockScreen") {
            BackHandler(enabled = true) {

            }
            LockScreen(navController = navController)
        }*/
    }
}