package com.example.taller3compumovil.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.taller3compumovil.screens.MapScreen
import com.example.taller3compumovil.screens.loginScreen
import com.example.taller3compumovil.screens.registerScreen
import com.example.taller3compumovil.screens.ProfileScreen
import com.example.taller3compumovil.viewModel.FirebaseViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavigationStack(
    modifier: Modifier = Modifier,
    accountViewModel: FirebaseViewModel = viewModel()
) {
    val navController = rememberNavController()
    val animationTime = 700

    val accountState by accountViewModel.uiState.collectAsState()

    LifecycleResumeEffect(Unit) {
//        if (accountState.user == null) {
//            navController.navigate(Routes.Login) {
//                popUpTo(Routes.Login) { inclusive = true }
//            }
//        }

        onPauseOrDispose {
        }
    }

    Scaffold {
        NavHost(
            navController = navController,
            startDestination = Routes.Login,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(animationTime)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(animationTime)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(animationTime)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(animationTime)
                )
            }
        ) {
            // This is the main navigation graph for the app
            val onSuccess = {
                navController.navigate(Routes.Authorized) {
                    popUpTo(Routes.Login) { inclusive = true }
                }
            }
            val logout = {
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Authorized) { inclusive = true }
                }
            }
            composable<Routes.Login> {
                loginScreen(
                    onLoginSuccess = onSuccess,
                    onRegisterClick = {
                        navController.navigate(Routes.Register)
                    },
                    viewModel = accountViewModel,
                    modifier = modifier
                )
            }
            composable<Routes.Register> {
                registerScreen(onRegisterSuccess = onSuccess,
                    viewModel = accountViewModel,
                    onAlreadyHasAccount = {
                        navController.navigate(Routes.Login)
                    })
            }
            // This is a nested navigation graph for authorized users
            navigation<Routes.Authorized>(startDestination = Routes.Home) {
                composable<Routes.Home> {
                    MapScreen(
                        navToProfile = {
                            navController.navigate(Routes.Profile)
                        },
                        navLogout = logout,
                        viewModel = accountViewModel
                    )
                }
                composable<Routes.Profile> {
                    ProfileScreen(
                        viewModel = accountViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}