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
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
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
import com.portablenotes.domain.models.events.EventsViewModel
import com.portablenotes.domain.models.events.Priority
import com.portablenotes.domain.models.events.hhmm
import com.portablenotes.domain.ui.AppColors
import com.portablenotes.domain.ui.AppTheme
import com.portablenotes.domain.ui.home.DayEvent
import com.portablenotes.domain.ui.navigation.AppBottomBar
import com.portablenotes.domain.ui.navigation.AppTab
import com.portablenotes.domain.ui.navigation.AuthRoutes
import com.portablenotes.domain.ui.navigation.OnboardingRoutes
import com.portablenotes.domain.ui.routines.LocalRoutines
import com.portablenotes.domain.ui.routines.RoutinesState
import com.portablenotes.domain.ui.screens.EventsScreen
import com.portablenotes.domain.ui.screens.ForgotPasswordScreen
import com.portablenotes.domain.ui.screens.HomeScreen
import com.portablenotes.domain.ui.screens.NotificationSettingsSheet
import com.portablenotes.domain.ui.events.icon
import com.portablenotes.domain.ui.screens.PermissionsScreen
import com.portablenotes.domain.ui.screens.LoginScreen
import com.portablenotes.domain.ui.screens.RegisterScreen
import com.portablenotes.domain.ui.screens.RoutinesScreen
import com.portablenotes.domain.ui.screens.TasksScreen
import com.portablenotes.domain.ui.tasks.TasksState
import androidx.compose.runtime.snapshotFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import com.portablenotes.notifications.EventAlarmScheduler
import com.portablenotes.notifications.MotivationScheduler
import com.portablenotes.notifications.NotificationChannels
import com.portablenotes.notifications.NotificationPrefs
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        NotificationChannels.ensure(this)
        MotivationScheduler.sync(this)          // mensajes motivadores (2 al día)
        setContent { App() }
    }
}

/**
 * Gesto de deslizar entre secciones. true: deslizar a la DERECHA lleva a la sección de la derecha
 * (Inicio → Eventos → Rutinas → Tareas) y a la IZQUIERDA a la anterior. false: al revés (como un carrusel).
 */
private const val SWIPE_RIGHT_GOES_NEXT = false
private val SWIPE_THRESHOLD = 72.dp

/** Primer nombre de la cuenta; si no hay, la parte del correo antes de la @; si tampoco, vacío. */
private fun firstNameOf(user: FirebaseUser?): String {
    val name = user?.displayName?.trim().orEmpty().substringBefore(' ')
    if (name.isNotBlank()) return name
    return user?.email?.substringBefore('@').orEmpty()
}

// ---------- Transiciones de navegación (rápidas: ~200 ms) ----------
private const val NAV_MS = 220
private const val FADE_MS = 140

private fun tabIndex(route: String?) = AppTab.entries.indexOfFirst { it.route == route }

/** Entre pestañas desliza según su posición en la barra; en el resto de pantallas, hacia la izquierda. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.direction(): AnimatedContentTransitionScope.SlideDirection {
    val from = tabIndex(initialState.destination.route)
    val to = tabIndex(targetState.destination.route)
    return if (from >= 0 && to >= 0 && to < from) {
        AnimatedContentTransitionScope.SlideDirection.Right
    } else {
        AnimatedContentTransitionScope.SlideDirection.Left
    }
}

private val navEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(direction(), tween(NAV_MS, easing = FastOutSlowInEasing), initialOffset = { it / 5 }) +
        fadeIn(tween(NAV_MS))
}
private val navExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(direction(), tween(NAV_MS, easing = FastOutSlowInEasing), targetOffset = { it / 5 }) +
        fadeOut(tween(FADE_MS))
}
private val navPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        tween(NAV_MS, easing = FastOutSlowInEasing),
        initialOffset = { it / 5 },
    ) + fadeIn(tween(NAV_MS))
}
private val navPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        tween(NAV_MS, easing = FastOutSlowInEasing),
        targetOffset = { it / 5 },
    ) + fadeOut(tween(FADE_MS))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun App() {
    val today = remember { LocalDate.now() }
    val context = LocalContext.current

    // ----- Autenticación -----
    val authVm: AuthViewModel = viewModel()
    val uid by authVm.uid.collectAsState()          // null = sin sesión
    val authUi by authVm.ui.collectAsState()

    // ----- Estados compartidos (arrancan vacíos: los llena Firestore al iniciar sesión) -----
    val eventsState = remember { EventsViewModel() }
    val routinesState = remember { RoutinesState() }
    val tasksState = remember { TasksState() }
    val cloud = remember { CloudStore() }

    // ----- Navegación -----
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = AppTab.entries.any { it.route == currentRoute }   // oculta la barra en Login/Registro
    val selectedTab = AppTab.entries.find { it.route == currentRoute } ?: AppTab.Home
    val startDestination = remember {
        when {
            !NotificationPrefs.permissionsShown(context) -> OnboardingRoutes.Permissions   // primer arranque
            authVm.uid.value != null -> AppTab.Home.route
            else -> AuthRoutes.Login
        }
    }

    // Al iniciar o cerrar sesión, lleva al usuario a la pantalla correcta
    LaunchedEffect(uid) {
        val route = navController.currentBackStackEntry?.destination?.route ?: return@LaunchedEffect
        if (route == OnboardingRoutes.Permissions) return@LaunchedEffect   // primero los permisos
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

    // Mantiene programados los recordatorios y alarmas de los eventos
    LaunchedEffect(uid) {
        if (uid == null) {
            EventAlarmScheduler.clearAll(context)
            return@LaunchedEffect
        }
        var loadedOnce = false   // evita borrar alarmas con la lista vacía previa a la sincronización
        snapshotFlow { eventsState.events.toList() }.collect { list ->
            if (list.isEmpty() && !loadedOnce) return@collect
            loadedOnce = true
            EventAlarmScheduler.sync(context, list)
        }
    }

    // Google: selector de cuentas → idToken → Firebase
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

    // Cambio de pestaña, usado por la barra inferior y por el gesto de deslizar
    val navigateToTab: (AppTab) -> Unit = { tab ->
        if (tab.route != currentRoute) {
            navController.navigate(tab.route) {
                popUpTo(AppTab.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    var showNotificationSettings by remember { mutableStateOf(false) }

    // Nombre de pila de la cuenta (el que se escribió al registrarse o el de Google)
    var userName by remember(uid) { mutableStateOf(firstNameOf(FirebaseAuth.getInstance().currentUser)) }
    LaunchedEffect(uid) {
        val user = FirebaseAuth.getInstance().currentUser ?: return@LaunchedEffect
        try {
            user.reload().await()          // al registrarse, el nombre se guarda un instante después de crear la cuenta
            userName = firstNameOf(FirebaseAuth.getInstance().currentUser)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) { /* sin conexión: se queda con el nombre local */ }
    }

    AppTheme {
        if (showNotificationSettings) {
            NotificationSettingsSheet(onDismiss = { showNotificationSettings = false })
        }
        CompositionLocalProvider(LocalRoutines provides routinesState.routines.toList()) {
            Scaffold(
                containerColor = AppColors.Background,
                bottomBar = {
                    if (showBottomBar) {
                        AppBottomBar(
                            selected = selectedTab,
                            onSelect = navigateToTab,
                        )
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier
                        .padding(innerPadding)
                        .pointerInput(currentRoute) {
                            // Solo en las 4 secciones principales (no en Login/Registro/Permisos).
                            // Los hijos que ya consumen el arrastre horizontal (listas de chips, etc.) tienen prioridad.
                            val index = AppTab.entries.indexOfFirst { it.route == currentRoute }
                            if (index < 0) return@pointerInput
                            val threshold = SWIPE_THRESHOLD.toPx()
                            var total = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { total = 0f },
                                onDragCancel = { total = 0f },
                                onHorizontalDrag = { _, dragAmount -> total += dragAmount },
                                onDragEnd = {
                                    if (kotlin.math.abs(total) >= threshold) {
                                        val toRight = total > 0
                                        val step = if (toRight == SWIPE_RIGHT_GOES_NEXT) 1 else -1
                                        AppTab.entries.getOrNull(index + step)?.let(navigateToTab)
                                    }
                                },
                            )
                        },
                    enterTransition = navEnter,
                    exitTransition = navExit,
                    popEnterTransition = navPopEnter,
                    popExitTransition = navPopExit,
                ) {
                    // ---------- Primer arranque ----------
                    composable(OnboardingRoutes.Permissions) {
                        PermissionsScreen(
                            onFinished = {
                                NotificationPrefs.setPermissionsShown(context)
                                val next = if (authVm.uid.value != null) AppTab.Home.route else AuthRoutes.Login
                                navController.navigate(next) { popUpTo(0) { inclusive = true } }
                            },
                        )
                    }

                    // ---------- Acceso ----------
                    composable(AuthRoutes.Login) {
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
                    composable(AuthRoutes.Register) {
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
                    composable(AuthRoutes.ForgotPassword) {
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
                    composable(AppTab.Home.route) {
                        HomeScreen(
                            today = today,
                            onOpenSettings = { showNotificationSettings = true },
                            userName = userName,
                            eventsFor = { date ->
                                eventsState.occurrencesOn(date).map {
                                    DayEvent(
                                        id = it.id,
                                        title = it.title,
                                        time = it.time.hhmm(),
                                        place = it.type.label,
                                        icon = it.type.icon,
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
                    composable(AppTab.Events.route) {
                        EventsScreen(eventsState, today = today)
                    }
                    composable(AppTab.Routines.route) {
                        RoutinesScreen(
                            state = routinesState,
                            linkedEvents = { r -> eventsState.events.count { it.routineId == r.id } },
                        )
                    }
                    composable(AppTab.Tasks.route) {
                        TasksScreen(tasksState)
                    }
                }
            }
        }
    }
}