package com.tcgvault.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tcgvault.app.data.local.model.PreconCardDetail
import com.tcgvault.app.data.local.model.PreconWithSummary
import com.tcgvault.app.data.local.model.SetWithProgress
import com.tcgvault.app.data.model.SetCategoryClassifier
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.ui.components.FavoriteStarButton
import com.tcgvault.app.ui.components.GameBadge
import com.tcgvault.app.ui.components.VaultSearchBar
import com.tcgvault.app.ui.components.gameAccentColor
import com.tcgvault.app.ui.theme.SubtleBorder
import com.tcgvault.app.ui.theme.TextPrimary
import com.tcgvault.app.ui.theme.TextSecondary
import com.tcgvault.app.ui.theme.VaultEmerald
import com.tcgvault.app.ui.theme.VaultGold
import com.tcgvault.app.ui.theme.VaultSurface
import com.tcgvault.app.ui.theme.VaultSurfaceElevated

private val UB_ACCENT = Color(0xFFB884FF)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetsScreen(
    selectedGame: TcgGame,
    sets: List<SetWithProgress>,
    precons: List<PreconWithSummary>,
    activePreconCards: List<PreconCardDetail>,
    isLoadingPreconCards: Boolean,
    addingPreconId: String?,
    query: String,
    isSyncing: Boolean,
    onQueryChange: (String) -> Unit,
    onSetClick: (String) -> Unit,
    onInspectPrecon: (String?) -> Unit,
    onAddPreconToCollection: (String) -> Unit,
    onRemovePreconFromCollection: (String) -> Unit,
    onCreateCustomPrecon: (String, TcgGame, String, Int, String, Boolean) -> Unit,
    onToggleSetFavorite: (String) -> Unit,
    onTogglePreconFavorite: (String) -> Unit
) {
    // Default to Booster Sets (Yu-Gi-Oh!) or Expansions (Magic)
    var selectedCategory by remember(selectedGame) {
        mutableStateOf<String?>(SetCategoryClassifier.defaultCategory(selectedGame))
    }
    var inspectedPrecon by remember { mutableStateOf<PreconWithSummary?>(null) }
    var showCreatePreconDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedGame) {
        selectedCategory = SetCategoryClassifier.defaultCategory(selectedGame)
    }

    val setComparator = remember {
        compareByDescending<SetWithProgress> { it.set.isFavorite }
            .thenByDescending { it.set.releaseDate }
            .thenBy { it.set.name }
    }

    val groupedSets = remember(selectedGame, sets) {
        val byCategory = sets.groupBy { item ->
            SetCategoryClassifier.categoryFor(item.set.game, item.set.setType, item.set.name)
        }
        val orderedKeys = SetCategoryClassifier.categoryOrder(selectedGame)
            .filter { byCategory.containsKey(it) } +
            byCategory.keys.filterNot { it in SetCategoryClassifier.categoryOrder(selectedGame) }
        orderedKeys.map { category ->
            category to byCategory.getValue(category).sortedWith(setComparator)
        }
    }

    // Universes Beyond (MTG only) — cross-cutting view; UB sets also stay in their normal categories
    val universesBeyondSets = remember(selectedGame, sets) {
        if (selectedGame != TcgGame.MTG) emptyList()
        else sets.filter { SetCategoryClassifier.isUniversesBeyond(it.set.game, it.set.code, it.set.name) }
            .sortedWith(setComparator)
    }

    val universesBeyondPrecons = remember(selectedGame, precons) {
        if (selectedGame != TcgGame.MTG) emptyList()
        else precons.filter { SetCategoryClassifier.isUniversesBeyond(it.precon.game, it.precon.setCode, it.precon.name) }
    }

    val isUbSelected = selectedCategory == SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY
    val visiblePrecons = when (selectedCategory) {
        null, SetCategoryClassifier.PRECONS_CATEGORY -> precons
        SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY -> universesBeyondPrecons
        else -> emptyList()
    }
    val showPreconsSection = visiblePrecons.isNotEmpty() || selectedCategory == SetCategoryClassifier.PRECONS_CATEGORY

    val visibleSetGroups = remember(groupedSets, selectedCategory, universesBeyondSets) {
        when (selectedCategory) {
            null -> groupedSets
            SetCategoryClassifier.PRECONS_CATEGORY -> emptyList()
            SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY ->
                if (universesBeyondSets.isNotEmpty()) {
                    listOf(SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY to universesBeyondSets)
                } else {
                    emptyList()
                }
            else -> groupedSets.filter { it.first == selectedCategory }
        }
    }

    val gameAccent = gameAccentColor(selectedGame)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TCG Sets",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Text(
                    text = "${sets.size} sets • ${precons.size} precons locally",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            AnimatedVisibility(
                visible = isSyncing,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(300))
            ) {
                Surface(
                    color = VaultSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, VaultGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = VaultGold
                        )
                        Text(
                            text = "Syncing…",
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        VaultSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Search sets, precons, or codes..."
        )

        if (groupedSets.isNotEmpty() || precons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Default primary categories first (Expansions for MTG, Booster Sets for YGO)
                items(groupedSets, key = { it.first }) { (category, itemsInCategory) ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = "$category (${itemsInCategory.size})",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = gameAccent.copy(alpha = 0.22f),
                            selectedLabelColor = gameAccent,
                            containerColor = VaultSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SubtleBorder,
                            selectedBorderColor = gameAccent
                        )
                    )
                }

                // Universes Beyond chip for Magic: The Gathering
                if (selectedGame == TcgGame.MTG) {
                    item(key = SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY) {
                        val ubCount = universesBeyondSets.size + universesBeyondPrecons.size
                        FilterChip(
                            selected = isUbSelected,
                            onClick = { selectedCategory = SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY },
                            label = {
                                Text(
                                    text = "Universes Beyond ($ubCount)",
                                    fontWeight = if (isUbSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = UB_ACCENT.copy(alpha = 0.22f),
                                selectedLabelColor = UB_ACCENT,
                                containerColor = VaultSurface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isUbSelected,
                                borderColor = SubtleBorder,
                                selectedBorderColor = UB_ACCENT
                            )
                        )
                    }
                }

                // Precons chip
                item(key = SetCategoryClassifier.PRECONS_CATEGORY) {
                    val isPrecons = selectedCategory == SetCategoryClassifier.PRECONS_CATEGORY
                    FilterChip(
                        selected = isPrecons,
                        onClick = { selectedCategory = SetCategoryClassifier.PRECONS_CATEGORY },
                        label = {
                            Text(
                                text = "Precons (${precons.size})",
                                fontWeight = if (isPrecons) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VaultGold.copy(alpha = 0.22f),
                            selectedLabelColor = VaultGold,
                            containerColor = VaultSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isPrecons,
                            borderColor = SubtleBorder,
                            selectedBorderColor = VaultGold
                        )
                    )
                }

                // All chip
                item(key = "ALL") {
                    val isAll = selectedCategory == null
                    FilterChip(
                        selected = isAll,
                        onClick = { selectedCategory = null },
                        label = {
                            Text(
                                text = "All (${sets.size + precons.size})",
                                fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = gameAccent.copy(alpha = 0.22f),
                            selectedLabelColor = gameAccent,
                            containerColor = VaultSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isAll,
                            borderColor = SubtleBorder,
                            selectedBorderColor = gameAccent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!showPreconsSection && visibleSetGroups.isEmpty()) {
            Surface(
                color = VaultSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No matching sets or precons found",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try a different search or tap 'Sync All Sets' to pull the full Yu-Gi-Oh! & MTG catalogs into local storage.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                visibleSetGroups.forEach { (category, categorySets) ->
                    item(key = "header:$category") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (category == SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY) UB_ACCENT else gameAccent
                            )
                            Text(
                                text = "${categorySets.size} ${if (categorySets.size == 1) "set" else "sets"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    items(categorySets, key = { it.set.id }) { item ->
                        SetListCard(
                            item = item,
                            onClick = { onSetClick(item.set.id) },
                            onToggleFavorite = { onToggleSetFavorite(item.set.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

                if (showPreconsSection) {
                    item(key = "header:precons") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isUbSelected) {
                                    "UNIVERSES BEYOND PRECONS (${visiblePrecons.size})"
                                } else {
                                    "PRECONS & BOXED SETS (${visiblePrecons.size})"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUbSelected) UB_ACCENT else VaultGold
                            )
                            TextButton(
                                onClick = { showCreatePreconDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.LibraryAdd,
                                    contentDescription = null,
                                    tint = VaultGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Custom", color = VaultGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    items(visiblePrecons, key = { it.precon.id }) { preconItem ->
                        PreconCard(
                            item = preconItem,
                            isAdding = addingPreconId == preconItem.precon.id,
                            onClick = {
                                inspectedPrecon = preconItem
                                onInspectPrecon(preconItem.precon.id)
                            },
                            onAdd = { onAddPreconToCollection(preconItem.precon.id) },
                            onRemove = { onRemovePreconFromCollection(preconItem.precon.id) },
                            onToggleFavorite = { onTogglePreconFavorite(preconItem.precon.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }

    if (inspectedPrecon != null) {
        val current = precons.firstOrNull { it.precon.id == inspectedPrecon?.precon?.id } ?: inspectedPrecon!!
        PreconDetailBottomSheet(
            current = current,
            cards = activePreconCards,
            isLoadingCards = isLoadingPreconCards,
            isAdding = addingPreconId == current.precon.id,
            onDismiss = {
                inspectedPrecon = null
                onInspectPrecon(null)
            },
            onAddPrecon = { onAddPreconToCollection(current.precon.id) }
        )
    }

    if (showCreatePreconDialog) {
        CreateCustomPreconDialog(
            initialGame = selectedGame,
            onDismiss = { showCreatePreconDialog = false },
            onSave = { name, game, category, year, cardLines, addNow ->
                onCreateCustomPrecon(name, game, category, year, cardLines, addNow)
                showCreatePreconDialog = false
            }
        )
    }
}

@Composable
private fun SetListCard(
    item: SetWithProgress,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val set = item.set
    val accent = gameAccentColor(set.game)
    val percent = (item.progressFraction * 100).toInt()
    val animatedProgress by animateFloatAsState(
        targetValue = item.progressFraction,
        animationSpec = tween(420),
        label = "setProgress"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            set.isFavorite -> VaultGold.copy(alpha = 0.75f)
            item.collectedUniqueCount > 0 -> accent.copy(alpha = 0.45f)
            else -> SubtleBorder
        },
        animationSpec = tween(250),
        label = "setCardBorder"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = VaultSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (set.isFavorite) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameBadge(game = set.game)
                    Surface(
                        color = VaultSurfaceElevated,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = set.code,
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = set.releaseDate.take(4),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = set.setType.replace("_", " ").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    FavoriteStarButton(
                        isFavorite = set.isFavorite,
                        onClick = onToggleFavorite,
                        sizeDp = 26
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = set.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.collectedUniqueCount} / ${item.displayTotalCards} collected",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.collectedUniqueCount > 0) VaultEmerald else TextSecondary
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (percent >= 100) VaultEmerald else VaultGold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (percent >= 100) VaultEmerald else accent,
                trackColor = VaultSurfaceElevated
            )
        }
    }
}
