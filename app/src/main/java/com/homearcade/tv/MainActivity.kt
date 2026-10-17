package com.homearcade.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.homearcade.tv.ui.navigation.AppNavigation
import com.homearcade.tv.ui.navigation.NavRoutes
import com.homearcade.tv.ui.theme.BgPrimary
import com.homearcade.tv.ui.theme.HomeArcadeTVTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as HomeArcadeTvApp

        setContent {
            HomeArcadeTVTheme {
                val serverUrl by app.preferences.serverUrl.collectAsState(initial = "LOADING")

                val navController = rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgPrimary)
                ) {
                    if (serverUrl != "LOADING") {
                        val startRoute = remember {
                            if (serverUrl.isNullOrEmpty()) NavRoutes.Setup.route
                            else NavRoutes.Home.route
                        }

                        AppNavigation(
                            navController = navController,
                            startDestination = startRoute
                        )
                    }
                }
            }
        }
    }
}
