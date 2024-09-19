package com.splicr.app

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.splicr.app.data.CanvasItemData
import com.splicr.app.ui.screens.ChooseUploadFormatScreen
import com.splicr.app.ui.screens.FeedbackSentScreen
import com.splicr.app.ui.screens.FullScreenMediaPlayerScreen
import com.splicr.app.ui.screens.HomeScreen
import com.splicr.app.ui.screens.ManageSubscriptionScreen
import com.splicr.app.ui.screens.MediaPlayerScreen
import com.splicr.app.ui.screens.MediaSplicedScreen
import com.splicr.app.ui.screens.NameYourProjectScreen
import com.splicr.app.ui.screens.OnboardingScreen
import com.splicr.app.ui.screens.PromptScreen
import com.splicr.app.ui.screens.ResetPasswordRequestSentScreen
import com.splicr.app.ui.screens.ResetPasswordScreen
import com.splicr.app.ui.screens.SettingsScreen
import com.splicr.app.ui.screens.SignInScreen
import com.splicr.app.ui.screens.SubscriptionScreen
import com.splicr.app.utils.ScreenOrientationUtil
import com.splicr.app.utils.SharedPreferenceUtil
import com.splicr.app.viewModel.HomeViewModel
import com.splicr.app.viewModel.SubscriptionViewModel

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = if (SharedPreferenceUtil.atHomeScreen()) {
        "HomeScreen"
    } else if (SharedPreferenceUtil.onboarded()) {
        "SubscriptionScreen"
    } else {
        "OnboardingScreen"
    },
    isDarkTheme: MutableState<Boolean>,
    subscriptionViewModel: SubscriptionViewModel
) {
    ScreenOrientationUtil.SetScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination, enterTransition = {
        slideInHorizontally(
            initialOffsetX = { it }, animationSpec = tween(durationMillis = 300)
        )
    }, exitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it }, animationSpec = tween(durationMillis = 300)
        )
    }, popEnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -it }, animationSpec = tween(durationMillis = 300)
        )
    }, popExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { it }, animationSpec = tween(durationMillis = 300)
        )
    }) {
        composable(route = "OnboardingScreen") {
            OnboardingScreen(
                isDarkTheme = isDarkTheme,
                navController = navController
            )
        }
        composable(route = "SubscriptionScreen") {
            BackHandler(enabled = true) {
                (context as Activity).moveTaskToBack(true)
            }
            SubscriptionScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                subscriptionViewModel = subscriptionViewModel
            )
        }
        composable(route = "HomeScreen") {
            BackHandler(enabled = true) {
                (context as Activity).moveTaskToBack(true)
            }
            HomeScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                homeViewModel = homeViewModel
            )
        }
        composable(route = "SettingsScreen") {
            SettingsScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                settingsViewModel = viewModel(),
                homeViewModel = homeViewModel
            )
        }
        composable(route = "SignInScreen") {
            SignInScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                signInViewModel = viewModel()
            )
        }
        composable(route = "ManageSubscriptionScreen") {
            ManageSubscriptionScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                subscriptionViewModel = subscriptionViewModel
            )
        }
        composable(route = "FeedbackSentScreen") {
            FeedbackSentScreen(isDarkTheme = isDarkTheme, navController = navController)
        }
        composable(route = "ChooseUploadFormatScreen") {
            ChooseUploadFormatScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                subscriptionViewModel = subscriptionViewModel
            )
        }
        composable(
            route = "PromptScreen/{uploadFormatStringResource}/{videoUriString}",
            arguments = listOf(navArgument("uploadFormatStringResource") {
                type = NavType.IntType
            }, navArgument("videoUriString") {
                type = NavType.StringType
            })
        ) {
            val uploadFormatStringResource =
                it.arguments?.getInt("uploadFormatStringResource") ?: -1
            val videoUriString = it.arguments?.getString("videoUriString") ?: ""
            PromptScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                uploadFormatStringResource = uploadFormatStringResource,
                promptViewModel = viewModel(),
                videoUriString = videoUriString,
                subscriptionViewModel = subscriptionViewModel
            )
        }
        composable(
            route = "MediaPlayerScreen/{canvasItemDataJson}/{videoUriString}",
            arguments = listOf(navArgument("canvasItemDataJson") {
                type = NavType.StringType
            }, navArgument("videoUriString") { type = NavType.StringType })
        ) {
            val canvasItemData = Gson().fromJson(
                it.arguments?.getString("canvasItemDataJson"), CanvasItemData::class.java
            )
            val videoUriString = it.arguments?.getString("videoUriString") ?: ""
            MediaPlayerScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                canvasItemData = canvasItemData,
                videoUriString = videoUriString
            )
        }
        composable(
            route = "FullScreenMediaPlayerScreen/{videoUriString}/{currentPosition}/{isPlaying}/{duration}",
            arguments = listOf(navArgument("videoUriString") { type = NavType.StringType },
                navArgument("currentPosition") { type = NavType.LongType },
                navArgument("isPlaying") { type = NavType.BoolType },
                navArgument("duration") { type = NavType.LongType })
        ) {
            ScreenOrientationUtil.SetScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            val videoUriString = it.arguments?.getString("videoUriString") ?: ""
            val currentPosition = it.arguments?.getLong("currentPosition") ?: 0
            val isPlaying = it.arguments?.getBoolean("isPlaying") ?: false
            val duration = it.arguments?.getLong("duration") ?: 0

            BackHandler(enabled = true) {
                (context as Activity).moveTaskToBack(true)
            }

            FullScreenMediaPlayerScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                videoUriString = videoUriString,
                currentPosition = currentPosition,
                isPlaying = isPlaying,
                duration = duration
            )
        }
        composable(
            route = "NameYourProjectScreen/{canvasItemDataJson}/{videoUriString}/{source}/{currentPosition}/{isPlaying}",
            arguments = listOf(navArgument("canvasItemDataJson") {
                type = NavType.StringType
            },
                navArgument("videoUriString") {
                    type = NavType.StringType
                },
                navArgument("source") { type = NavType.StringType },
                navArgument("currentPosition") { type = NavType.LongType },
                navArgument("isPlaying") { type = NavType.BoolType })
        ) {
            val canvasItemData = Gson().fromJson(
                it.arguments?.getString("canvasItemDataJson"), CanvasItemData::class.java
            )
            val videoUriString = it.arguments?.getString("videoUriString") ?: ""
            val currentPosition = it.arguments?.getLong("currentPosition") ?: 0
            val isPlaying = it.arguments?.getBoolean("isPlaying") ?: false

            BackHandler(enabled = true) {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "currentPosition", currentPosition
                )
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "isPlaying", isPlaying
                )
                navController.popBackStack()
            }

            NameYourProjectScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                canvasItemData = canvasItemData,
                videoUriString = videoUriString,
                currentPosition = currentPosition,
                isPlaying = isPlaying,
                nameYourProjectViewModel = viewModel()
            )
        }
        composable(
            route = "MediaSplicedScreen/{canvasItemDataJson}/{videoUriString}/{source}/{currentPosition}/{isPlaying}",
            arguments = listOf(navArgument("canvasItemDataJson") {
                type = NavType.StringType
            },
                navArgument("videoUriString") {
                    type = NavType.StringType
                },
                navArgument("source") { type = NavType.StringType },
                navArgument("currentPosition") { type = NavType.LongType },
                navArgument("isPlaying") { type = NavType.BoolType })
        ) {
            val canvasItemData = Gson().fromJson(
                it.arguments?.getString("canvasItemDataJson"), CanvasItemData::class.java
            )
            val videoUriString = it.arguments?.getString("videoUriString") ?: ""
            val source = it.arguments?.getString("source") ?: ""
            val currentPosition = it.arguments?.getLong("currentPosition") ?: 0
            val isPlaying = it.arguments?.getBoolean("isPlaying") ?: false

            BackHandler(enabled = true) {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "currentPosition", currentPosition
                )
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "isPlaying", isPlaying
                )
                navController.popBackStack()
            }

            MediaSplicedScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                canvasItemData = canvasItemData,
                videoUriString = videoUriString,
                source = source,
                currentPosition = currentPosition,
                isPlaying = isPlaying,
                mediaSplicedViewModel = viewModel(),
                subscriptionViewModel = subscriptionViewModel
            )
        }
        composable(route = "ResetPasswordScreen") {
            ResetPasswordScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                resetPasswordViewModel = viewModel()
            )
        }
        composable(
            route = "ResetPasswordRequestSentScreen/{email}",
            arguments = listOf(navArgument("email") {
                type = NavType.StringType
            })
        ) {
            BackHandler(enabled = true) {
                navController.popBackStack(route = "SignInScreen", inclusive = false)
            }
            val email = it.arguments?.getString("email") ?: ""
            ResetPasswordRequestSentScreen(
                isDarkTheme = isDarkTheme, navController = navController, email = email
            )
        }
    }
}