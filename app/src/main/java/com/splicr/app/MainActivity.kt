package com.splicr.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.splicr.app.viewModel.SubscriptionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val subscriptionViewModel: SubscriptionViewModel by viewModels {
                SubscriptionViewModelFactory(application) // Pass the application instance if needed
            }
            val isSystemInDarkTheme = true
            Navigation(isDarkTheme = remember {
                mutableStateOf(isSystemInDarkTheme)
            }, subscriptionViewModel = subscriptionViewModel)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class SubscriptionViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            return SubscriptionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}