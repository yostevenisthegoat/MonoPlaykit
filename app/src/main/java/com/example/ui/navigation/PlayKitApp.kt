package com.example.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.model.UtilityCategory
import com.example.data.model.UtilityIds
import com.example.ui.MainViewModel
import com.example.ui.components.MonoBackdrop
import com.example.ui.components.MonoBottomNavBar
import com.example.ui.components.NavTabItem
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MonoTheme
import com.example.ui.tools.boardgames.BingoCallerScreen
import com.example.ui.tools.boardgames.ChessClockScreen
import com.example.ui.tools.boardgames.DominoesCalcScreen
import com.example.ui.tools.boardgames.JengaSelectorScreen
import com.example.ui.tools.boardgames.LudoScreen
import com.example.ui.tools.boardgames.MonopolyBankScreen
import com.example.ui.tools.boardgames.ScrabbleToolScreen
import com.example.ui.tools.boardgames.SnakesLaddersScreen
import com.example.ui.tools.boardgames.TangramGuideScreen
import com.example.ui.tools.cardgames.PlayingCardPickerScreen
import com.example.ui.tools.cardgames.TcgCompanionScreen
import com.example.ui.tools.puzzles.CubeSolverScreen
import com.example.ui.tools.puzzles.CubeTimerScreen
import com.example.ui.tools.puzzles.PyraminxSolverScreen
import com.example.ui.tools.puzzles.SkewbSolverScreen
import com.example.ui.tools.puzzles.TwistyPuzzleGuidesScreen
import com.example.ui.tools.randomizers.CoinFlipScreen
import com.example.ui.tools.randomizers.DiceRollerScreen
import com.example.ui.tools.randomizers.PlayerTeamRandomizerScreen
import com.example.ui.tools.randomizers.RngScreen
import com.example.ui.tools.scoreboard.UniversalScoreboardScreen
import com.example.ui.tools.timers.CountdownTimerScreen
import com.example.ui.tools.timers.IntervalTimerScreen
import com.example.ui.tools.timers.MultiCounterScreen
import com.example.ui.tools.timers.StopwatchScreen
import com.example.ui.tools.toys.FidgetSpinnerScreen
import com.example.ui.tools.toys.KendamaTrackerScreen
import com.example.ui.tools.toys.LegoInventoryScreen
import com.example.ui.tools.toys.MetronomeScreen
import com.example.ui.tools.toys.ModelKitChecklistScreen
import com.example.ui.tools.toys.YoYoTrackerScreen
import kotlinx.coroutines.delay

object Routes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val UTILITY = "utility/{toolId}?hint={hint}"

    fun utility(toolId: String, hint: String? = null): String {
        val encoded = Uri.encode(hint.orEmpty())
        return "utility/$toolId?hint=$encoded"
    }
}

private val navTabs = listOf(
    NavTabItem(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavTabItem(Routes.EXPLORE, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    NavTabItem(Routes.FAVORITES, "Favorites", Icons.Filled.Star, Icons.Outlined.StarBorder),
    NavTabItem(Routes.HISTORY, "History", Icons.Filled.History, Icons.Outlined.History),
    NavTabItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun PlayKitApp(viewModel: MainViewModel) {
    val palette = MonoTheme.palette
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val favoriteTools by viewModel.favoriteTools.collectAsStateWithLifecycle()
    val recentTools by viewModel.recentTools.collectAsStateWithLifecycle()
    val history by viewModel.allHistory.collectAsStateWithLifecycle()

    var exploreCategoryFilter by remember { mutableStateOf<UtilityCategory?>(null) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(550L)
        showSplash = false
    }

    val isTopLevel = currentRoute in listOf(
        Routes.HOME,
        Routes.EXPLORE,
        Routes.FAVORITES,
        Routes.HISTORY,
        Routes.SETTINGS,
    )

    fun openTool(toolId: String, hint: String? = null) {
        viewModel.markToolOpened(toolId)
        navController.navigate(Routes.utility(toolId, hint))
    }

    fun selectTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    MonoBackdrop {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (isTopLevel && useRail) {
                    NavigationRail(
                        containerColor = palette.bgSecondary,
                        contentColor = palette.textPrimary,
                        modifier = Modifier.fillMaxHeight().statusBarsPadding(),
                    ) {
                        Spacer(Modifier.height(12.dp))
                        navTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { selectTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = palette.textPrimary,
                                    selectedTextColor = palette.textPrimary,
                                    unselectedIconColor = palette.textMuted,
                                    unselectedTextColor = palette.textMuted,
                                    indicatorColor = palette.surfaceSecondary,
                                ),
                            )
                        }
                    }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (isTopLevel && !useRail) {
                            MonoBottomNavBar(
                                tabs = navTabs,
                                currentRoute = currentRoute,
                                onSelectTab = ::selectTab,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                    ) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                favoriteTools = favoriteTools,
                                recentTools = recentTools,
                                onOpenTool = { id -> openTool(id) },
                                onOpenCategory = { cat ->
                                    exploreCategoryFilter = cat
                                    selectTab(Routes.EXPLORE)
                                },
                                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                                onOpenSettings = { selectTab(Routes.SETTINGS) },
                                onSeeAllExplore = {
                                    exploreCategoryFilter = null
                                    selectTab(Routes.EXPLORE)
                                },
                                onSeeAllFavorites = { selectTab(Routes.FAVORITES) },
                            )
                        }
                        composable(Routes.EXPLORE) {
                            ExploreScreen(
                                selectedCategory = exploreCategoryFilter,
                                onSelectCategory = { exploreCategoryFilter = it },
                                favoriteIds = favoriteIds,
                                onOpenTool = { id, hint -> openTool(id, hint) },
                                onToggleFavorite = viewModel::toggleFavorite,
                            )
                        }
                        composable(Routes.FAVORITES) {
                            FavoritesScreen(
                                favoriteTools = favoriteTools,
                                onOpenTool = { id -> openTool(id) },
                                onMoveFavorite = viewModel::moveFavorite,
                                onRemoveFavorite = viewModel::toggleFavorite,
                                onExploreClick = { selectTab(Routes.EXPLORE) },
                            )
                        }
                        composable(Routes.HISTORY) {
                            HistoryScreen(
                                records = history,
                                onDeleteRecord = viewModel::deleteHistoryRecord,
                                onClearAll = viewModel::clearAllHistory,
                                onOpenTool = { id -> openTool(id) },
                            )
                        }
                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                settings = settings,
                                viewModel = viewModel,
                            )
                        }
                        composable(Routes.SEARCH) {
                            SearchScreen(
                                recentSearches = settings.recentSearches,
                                favoriteIds = favoriteIds,
                                onBack = { navController.popBackStack() },
                                onOpenTool = { id, hint -> openTool(id, hint) },
                                onSaveSearch = viewModel::addRecentSearch,
                                onClearRecentSearches = viewModel::clearRecentSearches,
                                onToggleFavorite = viewModel::toggleFavorite,
                            )
                        }
                        composable(
                            route = Routes.UTILITY,
                            arguments = listOf(
                                navArgument("toolId") { type = NavType.StringType },
                                navArgument("hint") { type = NavType.StringType; defaultValue = "" },
                            ),
                        ) { entry ->
                            val toolId = entry.arguments?.getString("toolId") ?: UtilityIds.SCOREBOARD
                            val hint = entry.arguments?.getString("hint")?.takeIf { it.isNotBlank() }
                            val isFav = favoriteIds.contains(toolId)
                            val onBack: () -> Unit = { navController.popBackStack() }
                            val onFav: () -> Unit = { viewModel.toggleFavorite(toolId) }

                            when (toolId) {
                                UtilityIds.SCOREBOARD -> UniversalScoreboardScreen(
                                    viewModel = viewModel,
                                    isFavorite = isFav,
                                    defaultPresetKey = settings.defaultScoreboardPreset,
                                    defaultRedName = settings.defaultRedTeamName,
                                    defaultBlueName = settings.defaultBlueTeamName,
                                    keepScreenAwake = settings.keepScreenAwake,
                                    presetHint = hint,
                                    onBack = onBack,
                                    onToggleFavorite = onFav,
                                )
                                UtilityIds.CHESS_CLOCK -> ChessClockScreen(
                                    viewModel = viewModel,
                                    isFavorite = isFav,
                                    keepScreenAwake = settings.keepScreenAwake,
                                    onBack = onBack,
                                    onToggleFavorite = onFav,
                                )
                                UtilityIds.DICE_ROLLER -> DiceRollerScreen(
                                    viewModel = viewModel,
                                    isFavorite = isFav,
                                    saveToHistory = settings.saveDiceHistory,
                                    onBack = onBack,
                                    onToggleFavorite = onFav,
                                )
                                UtilityIds.COIN_FLIP -> CoinFlipScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.RNG_TOOL -> RngScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.TEAM_RANDOMIZER -> PlayerTeamRandomizerScreen(viewModel, isFav, onBack, onFav)

                                UtilityIds.CUBE_TIMER -> CubeTimerScreen(
                                    viewModel = viewModel,
                                    isFavorite = isFav,
                                    defaultInspection = settings.cubeInspectionEnabled,
                                    keepScreenAwake = settings.keepScreenAwake,
                                    onBack = onBack,
                                    onToggleFavorite = onFav,
                                )
                                UtilityIds.CUBE_SOLVER -> CubeSolverScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.PYRAMINX_SOLVER -> PyraminxSolverScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.SKEWB_SOLVER -> SkewbSolverScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.PUZZLE_GUIDES -> TwistyPuzzleGuidesScreen(isFav, onBack, onFav)

                                UtilityIds.MONOPOLY_BANK -> MonopolyBankScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.SCRABBLE_TOOL -> ScrabbleToolScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.LUDO_TOOL -> LudoScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.SNAKES_LADDERS -> SnakesLaddersScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.DOMINOES_CALC -> DominoesCalcScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.BINGO_CALLER -> BingoCallerScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.JENGA_SELECTOR -> JengaSelectorScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.TANGRAM_GUIDE -> TangramGuideScreen(isFav, onBack, onFav)

                                UtilityIds.TCG_COMPANION -> TcgCompanionScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.CARD_PICKER -> PlayingCardPickerScreen(viewModel, isFav, onBack, onFav)

                                UtilityIds.FIDGET_SPINNER -> FidgetSpinnerScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.YOYO_TRACKER -> YoYoTrackerScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.KENDAMA_TRACKER -> KendamaTrackerScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.LEGO_INVENTORY -> LegoInventoryScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.MODEL_KIT_CHECKLIST -> ModelKitChecklistScreen(viewModel, isFav, onBack, onFav)
                                UtilityIds.METRONOME -> MetronomeScreen(viewModel, isFav, settings.keepScreenAwake, onBack, onFav)

                                UtilityIds.STOPWATCH -> StopwatchScreen(viewModel, isFav, settings.keepScreenAwake, onBack, onFav)
                                UtilityIds.COUNTDOWN_TIMER -> CountdownTimerScreen(viewModel, isFav, settings.keepScreenAwake, onBack, onFav)
                                UtilityIds.INTERVAL_TIMER -> IntervalTimerScreen(viewModel, isFav, settings.keepScreenAwake, onBack, onFav)
                                UtilityIds.MULTI_COUNTER -> MultiCounterScreen(viewModel, isFav, onBack, onFav)
                            }
                        }
                    }
                }
            }

            // Minimal Mono Branded Splash Overlay
            AnimatedVisibility(
                visible = showSplash,
                exit = fadeOut(tween(260)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(palette.bgMain),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_playkit_mark_1795354177166),
                            contentDescription = "Mono PlayKit emblem",
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.dp, palette.border, RoundedCornerShape(24.dp)),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "MONO PLAYKIT",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Your Games. Your Tools.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
