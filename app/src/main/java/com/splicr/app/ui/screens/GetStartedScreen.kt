package com.splicr.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.splicr.app.ui.theme.SplicrTheme

@Composable
fun GetStartedScreen(navController: NavHostController) {
    SplicrTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {

        }
    }

}

@Composable
@PreviewLightDark
fun GetStartedScreenPreview() {
    GetStartedScreen(navController = rememberNavController())
}