package com.splicr.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.appdistribution.FirebaseAppDistribution
import com.google.firebase.appdistribution.InterruptionLevel
import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.ktx.Firebase
import com.splicr.app.viewModel.SplashScreenViewModel

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showFeedbackNotification()
        } else {
            Toast.makeText(
                this,
                getString(R.string.the_notifications_permissions_is_required_for_your_feedback),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val splashScreenViewModel: SplashScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashScreenViewModel.isLoading.value
            }
            setOnExitAnimationListener {
                it.remove()
                enableEdgeToEdge()
                WindowInsetsControllerCompat(
                    window, window.decorView
                ).isAppearanceLightStatusBars = false
                WindowInsetsControllerCompat(
                    window, window.decorView
                ).isAppearanceLightNavigationBars = false
                FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            showFeedbackNotification()
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                        ) -> {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.the_notifications_permissions_is_required_for_your_feedback),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            requestPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                } else {
                    showFeedbackNotification()
                }
            }
        }
        super.onCreate(savedInstanceState)
        setContent {
            Navigation()
        }
    }

    private fun showFeedbackNotification() {
        Firebase.appDistribution.showFeedbackNotification(
            getString(R.string.send_a_feedback_to_the_team, getString(R.string.in_app_name)),
            InterruptionLevel.HIGH
        )
    }
}