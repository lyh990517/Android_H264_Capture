package com.example.myapplication

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AppNav(
    activity: MainActivity,
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(navController = navHostController, startDestination = "Capture") {
        composable("Capture") {
            CaptureScreen {
                activity.finish()
                Toast.makeText(activity, "Start Capture.", Toast.LENGTH_SHORT).show()
            }
        }
        composable("CaptureVideos") {
            CaptureVideosScreen()
        }
    }
}