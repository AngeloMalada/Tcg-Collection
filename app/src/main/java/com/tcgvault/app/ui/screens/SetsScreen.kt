package com.tcgvault.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tcgvault.app.data.local.model.PreconCardDetail
import com.tcgvault.app.data.local.model.PreconWithSummary
import com.tcgvault.app.data.local.model.SetWithProgress
import com.tcgvault.app.data.model.SetCategoryClassifier
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.ui.components.FavoriteStarButton
import com.tcgvault.app.ui.components.VaultSearchBar
import com.tcgvault.app.ui.theme.MonoFont
import com.tcgvault.app.ui.theme.SubtleBorder
import com.tcgvault.app.ui.theme.TextMuted
import com.tcgvault.app.ui.theme.TextPrimary
import com.tcgvault.app.ui.theme.TextSecondary
import com.tcgvault.app.ui.theme.VaultAccentWhite
import com.tcgvault.app.ui.theme.VaultBackground
import com.tcgvault.app.ui.theme.VaultEmerald
import com.tcgvault.app.ui.theme.VaultGold
import com.tcgvault.app.ui.theme.VaultSurface
import com.tcgvault.app.ui.theme.VaultSurfaceElevated

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetsScreen(
    selectedGame: TcgGame,
    selectedCategory: String?,
    sets: List<SetWithProgress>,
    precons: List<PreconWithSummary>,
    activePreconCards: List<PreconCardDetail>,
    isLoadingPreconCards: Boolean,
    addingPreconId: String?,
    query: String,
    isSyncing: Boolean,
    onSelectCategory: (String?) -> Unit,
    onQueryChange: (String) -> Unit,
    onSetClick: (String) -> Unit,
    onInspectPrecon: (String?) -> Unit,
    onAddPreconToCollection: (String) -> Unit,
    onRemovePreconFromCollection: (String) -> Unit,
    onCreateCustomPrecon: (String, TcgGame, String, Int, String, Boolean) -> Unit,
    onToggleSetFavorite: (String) -> Unit,
    onTogglePreconFavorite: (String) -> Unit,
    bottomContentPadding: Dp = 24.dp
) {
    var inspectedPrecon by remember { mutableStateOf<PreconWithSummary?>(null) }
    var showCreatePreconDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        VaultSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Search sets, precons, or codes..."
        )

        if (groupedSets.isNotEmpty() || precons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(groupedSets, key = { it.first }) { (category, itemsInCategory) ->
                    CategoryPill(
                        label = category,
                        count = itemsInCategory.size,
                        selected = selectedCategory == category,
                        onClick = { onSelectCategory(category) }
                    )
                }

                if (selectedGame == TcgGame.MTG) {
                    item(key = SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY) {
                        CategoryPill(
                            label = "Universes Beyond",
                            count = universesBeyondSets.size + universesBeyondPrecons.size,
                            selected = isUbSelected,
                            onClick = { onSelectCategory(SetCategoryClassifier.UNIVERSES_BEYOND_CATEGORY) }
                        )
                    }
                }

                item(key = SetCategoryClassifier.PRECONS_CATEGORY) {
                    CategoryPill(
                        label = "Precons",
                        count = precons.size,
                        selected = selectedCategory == SetCategoryClassifier.PRECONS_CATEGORY,
                        onClick = { onSelectCategory(SetCategoryClassifier.PRECONS_CATEGORY) }
                    )
                }

                item(key = "ALL") {
                    CategoryPill(
                        label = "All",
                        count = sets.size + precons.size,
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!showPreconsSection && visibleSetGroups.isEmpty()) {
            Surface(
                color = VaultSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SubtleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSyncing) "Syncing catalog…" else "No matching sets or precons",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try a different search or category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = bottomContentPadding)
            ) {
                visibleSetGroups.forEach { (category, categorySets) ->
                    item(key = "header:$category") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.uppercase(),
                                fontFamily = MonoFont,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                            Text(
                                text = "${categorySets.size}",
                                fontFamily = MonoFont,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
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
                                .padding(top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isUbSelected) {
                                    "UNIVERSES BEYOND PRECONS · ${visiblePrecons.size}"
                                } else {
                                    "PRECONS & BOXED SETS · ${visiblePrecons.size}"
                                },
                                fontFamily = MonoFont,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showCreatePreconDialog = true },
                                color = VaultSurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, SubtleBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Custom",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
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
private fun CategoryPill(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) VaultAccentWhite else VaultSurface,
        animationSpec = tween(180),
        label = "catBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) VaultBackground else TextSecondary,
        animationSpec = tween(180),
        label = "catText"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) VaultAccentWhite else SubtleBorder,
        animationSpec = tween(180),
        label = "catBorder"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
            Text(
                text = "$count",
                fontFamily = MonoFont,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) VaultBackground.copy(alpha = 0.65f) else TextMuted
            )
        }
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
    val percent = (item.progressFraction * 100).toInt()
    val animatedProgress by animateFloatAsState(
        targetValue = item.progressFraction,
        animationSpec = tween(380),
        label = "setProgress"
    )
    val borderColor by animateColorAsState(
        targetValue = if (set.isFavorite) VaultGold.copy(alpha = 0.55f) else SubtleBorder,
        animationSpec = tween(220),
        label = "setCardBorder"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = VaultSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = VaultSurfaceElevated,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Text(
                            text = set.code,
                            fontFamily = MonoFont,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = set.releaseDate.take(4),
                        fontFamily = MonoFont,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "· ${set.setType.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FavoriteStarButton(
                    isFavorite = set.isFavorite,
                    onClick = onToggleFavorite,
                    sizeDp = 24
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = set.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (item.collectedUniqueCount > 0) VaultEmerald else SubtleBorder,
                    trackColor = VaultSurfaceElevated
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${item.collectedUniqueCount}/${item.displayTotalCards}",
                    fontFamily = MonoFont,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.collectedUniqueCount > 0) VaultEmerald else TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$percent%",
                    fontFamily = MonoFont,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (percent >= 100) VaultEmerald else TextSecondary
                )
            }
        }
    }
}
