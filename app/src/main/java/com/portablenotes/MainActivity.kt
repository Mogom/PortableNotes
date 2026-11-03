package com.portablenotes

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.portablenotes.domain.models.auth.AuthViewModel
import com.portablenotes.domain.models.auth.GoogleAuthClient
import com.portablenotes.data.remote.CloudStore                       // ← ajusta al paquete donde lo pusiste
import com.portablenotes.domain.models.Priority
import com.portablenotes.domain.models.hhmm
import com.portablenotes.domain.ui.AppColors
import com.portablenotes.domain.ui.AppTheme
import com.portablenotes.domain.models.EventsState
import com.portablenotes.domain.ui.home.DayEvent
import com.portablenotes.domain.ui.navigation.AppBottomBar
import com.portablenotes.domain.ui.navigation.AppTab
import com.portablenotes.domain.ui.navigation.AuthRoutes
import com.portablenotes.domain.ui.routines.LocalRoutines
import com.portablenotes.domain.ui.routines.RoutinesState
import com.portablenotes.domain.ui.screens.EventsScreen
import com.portablenotes.domain.ui.screens.ForgotPasswordScreen
import com.portablenotes.domain.ui.screens.HomeScreen
import com.portablenotes.domain.ui.screens.LoginScreen
import com.portablenotes.domain.ui.screens.RegisterScreen
import com.portablenotes.domain.ui.screens.RoutinesScreen
import com.portablenotes.domain.ui.screens.TasksScreen
import com.portablenotes.domain.ui.tasks.TasksState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent { App() }
    }
}

private val slideLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
    { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) }
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun App() {
    val today = remember { LocalDate.now() }

    // ----- Autenticación -----
    val authVm: AuthViewModel = viewModel()
    val uid by authVm.uid.collectAsState()          // null = sin sesión
    val authUi by authVm.ui.collectAsState()

    // ----- Estados compartidos (arrancan vacíos: los llena Firestore al iniciar sesión) -----
    val eventsState = remember { EventsState() }
    val routinesState = remember { RoutinesState() }
    val tasksState = remember { TasksState() }
    val cloud = remember { CloudStore() }

    // ----- Navegación -----
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = AppTab.entries.any { it.route == currentRoute }   // oculta la barra en Login/Registro
    val selectedTab = AppTab.entries.find { it.route == currentRoute } ?: AppTab.Home
    val startDestination = remember { if (authVm.uid.value != null) AppTab.Home.route else AuthRoutes.Login }

    // Al iniciar o cerrar sesión, lleva al usuario a la pantalla correcta
    LaunchedEffect(uid) {
        val route = navController.currentBackStackEntry?.destination?.route ?: return@LaunchedEffect
        val onAuthScreen = route in AuthRoutes.all
        if (uid != null && onAuthScreen) {
            navController.navigate(AppTab.Home.route) { popUpTo(0) { inclusive = true } }
        } else if (uid == null && !onAuthScreen) {
            navController.navigate(AuthRoutes.Login) { popUpTo(0) { inclusive = true } }
        }
    }

    // Sincronización con Firestore mientras haya sesión; al cerrar sesión se detiene y se limpia
    LaunchedEffect(uid) {
        val id = uid ?: return@LaunchedEffect
        try {
            cloud.start(id, this, eventsState.events, tasksState.tasks, routinesState.routines)
            awaitCancellation()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Cloud", "No se pudo conectar con Firestore", e)
        } finally {
            cloud.stop()
            eventsState.events.clear()
            tasksState.tasks.clear()
            routinesState.routines.clear()
        }
    }

    // Google: selector de cuentas → idToken → Firebase
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onGoogleSignIn: () -> Unit = {
        scope.launch {
            try {
                val token = GoogleAuthClient.getIdToken(context, context.getString(R.string.default_web_client_id))
                authVm.signInWithGoogle(token)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                authVm.onGoogleError(e)
            }
        }
    }

    AppTheme {
        CompositionLocalProvider(LocalRoutines provides routinesState.routines.toList()) {
            Scaffold(
                containerColor = AppColors.Background,
                bottomBar = {
                    if (showBottomBar) {
                        AppBottomBar(
                            selected = selectedTab,
                            onSelect = { tab ->
                                if (tab.route != currentRoute) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        )
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    // ---------- Acceso ----------
                    composable(AuthRoutes.Login, enterTransition = slideLeft) {
                        LoginScreen(
                            onLogin = { email, password -> authVm.login(email, password) },
                            onGoogleSignIn = onGoogleSignIn,
                            onForgotPassword = {
                                authVm.clearStatus()
                                navController.navigate(AuthRoutes.ForgotPassword)
                            },
                            onGoToRegister = {
                                authVm.clearStatus()
                                navController.navigate(AuthRoutes.Register)
                            },
                            isLoading = authUi.isLoading,
                            errorMessage = authUi.errorMessage,
                        )
                    }
                    // parámetros análogos a LoginScreen; ajústalos a tus firmas reales.
                    composable(AuthRoutes.Register, enterTransition = slideLeft) {
                        RegisterScreen(
                            onRegister = { name, email, password -> authVm.register(email, password, name) },
                            onGoogleSignIn = onGoogleSignIn,
                            onGoToLogin = {
                                authVm.clearStatus()
                                navController.popBackStack()
                            },
                            isLoading = authUi.isLoading,
                            errorMessage = authUi.errorMessage,
                        )
                    }
                    composable(AuthRoutes.ForgotPassword, enterTransition = slideLeft) {
                        ForgotPasswordScreen(
                            onSendResetLink = { email -> authVm.sendPasswordReset(email) },
                            onBackToLogin = {
                                authVm.clearStatus()
                                navController.popBackStack()
                            },
                            isLoading = authUi.isLoading,
                            errorMessage = authUi.errorMessage,
                            emailSent = authUi.resetEmailSent,
                        )
                    }

                    // ---------- App ----------
                    composable(AppTab.Home.route, enterTransition = slideLeft) {
                        HomeScreen(
                            today = today,
                            eventsFor = { date ->
                                eventsState.occurrencesOn(date).map {
                                    DayEvent(
                                        id = it.id,
                                        title = it.title,
                                        time = it.time.hhmm(),
                                        place = it.type.label,
                                        tint = when (it.priority) {
                                            Priority.High -> AppColors.Tint2
                                            Priority.Medium -> AppColors.Tint1
                                            Priority.Low -> AppColors.Tint3
                                        },
                                    )
                                }
                            },
                        )
                    }
                    composable(AppTab.Events.route, enterTransition = slideLeft) {
                        EventsScreen(state = eventsState, today = today)
                    }
                    composable(AppTab.Routines.route, enterTransition = slideLeft) {
                        RoutinesScreen(
                            state = routinesState,
                            linkedEvents = { r -> eventsState.events.count { it.routineId == r.id } },
                        )
                    }
                    composable(AppTab.Tasks.route, enterTransition = slideLeft) {
                        TasksScreen(tasksState)
                    }
                }
            }
        }
    }
}