package com.tcgvault.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tcgvault.app.ui.components.TopLeftGameSelector
import com.tcgvault.app.ui.screens.CollectionScreen
import com.tcgvault.app.ui.screens.SetDetailScreen
import com.tcgvault.app.ui.screens.SetsScreen
import com.tcgvault.app.ui.theme.TextPrimary
import com.tcgvault.app.ui.theme.TextSecondary
import com.tcgvault.app.ui.theme.TcgVaultTheme
import com.tcgvault.app.ui.theme.VaultBackground
import com.tcgvault.app.ui.theme.VaultGold
import com.tcgvault.app.ui.theme.VaultSurface
import com.tcgvault.app.ui.theme.VaultSurfaceElevated
import com.tcgvault.app.ui.viewmodel.TcgViewModel
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        val app = application as TcgVaultApp
        setContent {
            TcgVaultTheme {
                val vm: TcgViewModel = viewModel(
                    factory = TcgViewModel.Factory(
                        repository = app.repository,
                        initialGame = app.getLastSelectedGame(),
                        onGamePersisted = app::saveLastSelectedGame
                    )
                )
                TcgVaultRoot(viewModel = vm)
            }
        }
    }
}

private enum class TopLevelTab(val route: String, val label: String, val icon: ImageVector) {
    SETS("sets", "Sets", Icons.Default.Layers),
    COLLECTION("collection", "Collection", Icons.Default.CollectionsBookmark)
}

@Composable
private fun TcgVaultRoot(viewModel: TcgViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedGame by viewModel.selectedGame.collectAsState()
    val setsQuery by viewModel.setsQuery.collectAsState()
    val sets by viewModel.sets.collectAsState()
    val isSyncing by viewModel.isSyncingCatalog.collectAsState()

    val precons by viewModel.precons.collectAsState()
    val activePreconCards by viewModel.activePreconCards.collectAsState()
    val isLoadingPreconCards by viewModel.isLoadingPreconCards.collectAsState()
    val addingPreconId by viewModel.isAddingPreconId.collectAsState()

    val collectionQuery by viewModel.collectionQuery.collectAsState()
    val ownedCards by viewModel.ownedCards.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val activeSet by viewModel.activeSet.collectAsState()
    val activeSetCards by viewModel.activeSetCards.collectAsState()
    val setCardQuery by viewModel.setCardQuery.collectAsState()
    val setCardFilter by viewModel.setCardFilter.collectAsState()
    val isLoadingSetCards by viewModel.isLoadingSetCards.collectAsState()
    val setLoadError by viewModel.setLoadError.collectAsState()

    val statusBanner by viewModel.statusBanner.collectAsState()

    LaunchedEffect(statusBanner) {
        statusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissBanner()
        }
    }

    val showBottomBar = TopLevelTab.entries.any { it.route == currentRoute }

    Scaffold(
        containerColor = VaultBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (showBottomBar) {
                Surface(color = VaultBackground) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TopLeftGameSelector(
                            selected = selectedGame,
                            onSelect = viewModel::selectGame
                        )
                        Text(
                            text = "TCG Vault",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VaultGold
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = VaultSurface) {
                    TopLevelTab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.route) {
                                        popUpTo(TopLevelTab.SETS.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VaultGold,
                                selectedTextColor = VaultGold,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = VaultSurfaceElevated
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VaultBackground)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = TopLevelTab.SETS.route,
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 12 } },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 12 } },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 10 } }
            ) {
                composable(TopLevelTab.SETS.route) {
                    SetsScreen(
                        selectedGame = selectedGame,
                        sets = sets,
                        precons = precons,
                        activePreconCards = activePreconCards,
                        isLoadingPreconCards = isLoadingPreconCards,
                        addingPreconId = addingPreconId,
                        query = setsQuery,
                        isSyncing = isSyncing,
                        onQueryChange = viewModel::updateSetsQuery,
                        onSetClick = { setId ->
                            viewModel.openSet(setId)
                            val encoded = URLEncoder.encode(setId, "UTF-8")
                            navController.navigate("set/$encoded")
                        },
                        onInspectPrecon = viewModel::selectPreconForDetail,
                        onAddPreconToCollection = viewModel::addPreconToCollection,
                        onRemovePreconFromCollection = viewModel::removePreconFromCollection,
                        onCreateCustomPrecon = viewModel::createCustomPrecon,
                        onToggleSetFavorite = viewModel::toggleSetFavorite,
                        onTogglePreconFavorite = viewModel::togglePreconFavorite
                    )
                }

                composable(
                    route = "set/{setId}",
                    arguments = listOf(navArgument("setId") { type = NavType.StringType })
                ) { entry ->
                    val encodedId = entry.arguments?.getString("setId").orEmpty()
                    val decodedId = remember(encodedId) { URLDecoder.decode(encodedId, "UTF-8") }
                    LaunchedEffect(decodedId) {
                        if (activeSet?.set?.id != decodedId) {
                            viewModel.openSet(decodedId)
                        }
                    }
                    SetDetailScreen(
                        setProgress = activeSet,
                        cards = activeSetCards,
                        query = setCardQuery,
                        ownershipFilter = setCardFilter,
                        isLoadingCards = isLoadingSetCards,
                        loadError = setLoadError,
                        onBack = { navController.popBackStack() },
                        onQueryChange = viewModel::updateSetCardQuery,
                        onFilterChange = viewModel::updateSetCardFilter,
                        onCardQuickAdd = viewModel::onCardQuickAdd,
                        onAdjustQuantity = viewModel::adjustCardQuantity,
                        onToggleCardFavorite = viewModel::toggleCardFavorite
                    )
                }

                composable(TopLevelTab.COLLECTION.route) {
                    CollectionScreen(
                        stats = stats,
                        ownedCards = ownedCards,
                        query = collectionQuery,
                        onQueryChange = viewModel::updateCollectionQuery,
                        onAdjustQuantity = viewModel::adjustCardQuantity,
                        onToggleCardFavorite = viewModel::toggleCardFavorite
                    )
                }
            }
        }
    }
}
