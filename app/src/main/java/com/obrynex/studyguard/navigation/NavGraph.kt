package com.obrynex.studyguard.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.obrynex.studyguard.ai.AiTutorScreen
import com.obrynex.studyguard.ai.AiTutorViewModel
import com.obrynex.studyguard.data.prefs.PrefsManager
import com.obrynex.studyguard.islamic.ui.IslamicScreen
import com.obrynex.studyguard.islamic.ui.IslamicViewModel
import com.obrynex.studyguard.summarizer.ui.SummarizerScreen
import com.obrynex.studyguard.summarizer.ui.SummarizerViewModel
import com.obrynex.studyguard.tracker.SessionDetailScreen
import com.obrynex.studyguard.tracker.SessionDetailViewModel
import com.obrynex.studyguard.tracker.TrackerScreen
import com.obrynex.studyguard.tracker.TrackerViewModel
import com.obrynex.studyguard.ui.onboarding.OnboardingScreen
import com.obrynex.studyguard.ui.theme.*
import com.obrynex.studyguard.wellbeing.WellbeingScreen
import com.obrynex.studyguard.wellbeing.WellbeingViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private object Routes {
    const val ONBOARDING     = "onboarding"
    const val TRACKER        = "tracker"
    const val SUMMARIZER     = "summarizer"
    const val WELLBEING      = "wellbeing"
    const val ISLAMIC        = "islamic"
    const val AI_TUTOR       = "ai_tutor"
    const val SESSION_DETAIL = "session_detail/{sessionId}"
    fun sessionDetail(id: Long) = "session_detail/$id"
}

private sealed class Tab(val route: String, val label: String,
                         val icon: ImageVector, val iconSelected: ImageVector) {
    object Tracker   : Tab(Routes.TRACKER,    "المذاكرة",  Icons.Outlined.Timer,          Icons.Filled.Timer)
    object Summarizer: Tab(Routes.SUMMARIZER, "تلخيص",     Icons.Outlined.AutoStories,    Icons.Filled.AutoStories)
    object AiTutor   : Tab(Routes.AI_TUTOR,   "مساعد",     Icons.Outlined.Psychology,     Icons.Filled.Psychology)
    object Islamic   : Tab(Routes.ISLAMIC,    "أذكار",     Icons.Outlined.AutoAwesome,    Icons.Filled.AutoAwesome)
    object Wellbeing : Tab(Routes.WELLBEING,  "الشاشة",    Icons.Outlined.PhoneAndroid,   Icons.Filled.PhoneAndroid)
}

private val TABS = listOf(Tab.Tracker, Tab.Summarizer, Tab.AiTutor, Tab.Islamic, Tab.Wellbeing)
private val BOTTOM_NAV_ROUTES = TABS.map { it.route }.toSet()

@Composable
fun NavGraph() {
    val ctx           = LocalContext.current
    val navController = rememberNavController()
    val backStack     by navController.currentBackStackEntryAsState()
    val currentRoute  = backStack?.destination?.route
    val scope         = rememberCoroutineScope()

    var startDest by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val done  = PrefsManager.onboardingDone(ctx).first()
        startDest = if (done) Routes.TRACKER else Routes.ONBOARDING
    }
    if (startDest == null) return

    val showBottomBar = currentRoute in BOTTOM_NAV_ROUTES

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = BgDark, tonalElevation = 0.dp) {
                    HorizontalDivider(color = Divider, thickness = 0.5.dp)
                    TABS.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            },
                            icon  = { Icon(if (selected) tab.iconSelected else tab.icon, tab.label, modifier = androidx.compose.ui.Modifier.size(22.dp)) },
                            label = { Text(tab.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = TextPrimary, selectedTextColor   = TextPrimary,
                                unselectedIconColor = TextMuted,   unselectedTextColor = TextMuted,
                                indicatorColor      = Surface2
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = startDest!!,
            androidx.compose.ui.Modifier.padding(padding)) {

            composable(Routes.ONBOARDING) {
                OnboardingScreen {
                    scope.launch { PrefsManager.setOnboardingDone(ctx) }
                    navController.navigate(Routes.TRACKER) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            }

            composable(Routes.TRACKER) {
                val vm: TrackerViewModel = viewModel(factory = TrackerViewModel.Factory)
                TrackerScreen(vm = vm, onSessionClick = { session ->
                    navController.navigate(Routes.sessionDetail(session.id))
                })
            }

            composable(Routes.SUMMARIZER) {
                val vm: SummarizerViewModel = viewModel(factory = SummarizerViewModel.Factory)
                SummarizerScreen(vm)
            }

            composable(Routes.AI_TUTOR) {
                val vm: AiTutorViewModel = viewModel(factory = AiTutorViewModel.Factory)
                AiTutorScreen(vm)
            }

            composable(Routes.WELLBEING) {
                val vm: WellbeingViewModel = viewModel(factory = WellbeingViewModel.Factory)
                WellbeingScreen(vm)
            }

            composable(Routes.ISLAMIC) {
                val vm: IslamicViewModel = viewModel(factory = IslamicViewModel.Factory)
                IslamicScreen(vm)
            }

            composable(
                route     = Routes.SESSION_DETAIL,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                val vm: SessionDetailViewModel = viewModel(factory = SessionDetailViewModel.Factory)
                val session by vm.sessionById(sessionId).collectAsState(initial = null)
                session?.let {
                    SessionDetailScreen(
                        session  = it,
                        onBack   = { navController.popBackStack() },
                        onDelete = vm::delete
                    )
                }
            }
        }
    }
}
