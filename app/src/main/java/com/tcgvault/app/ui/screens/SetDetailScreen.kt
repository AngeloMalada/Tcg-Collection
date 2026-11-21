package com.tcgvault.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tcgvault.app.data.local.model.CardWithCollection
import com.tcgvault.app.data.local.model.SetWithProgress
import com.tcgvault.app.ui.components.AnimatedCounterText
import com.tcgvault.app.ui.components.CardDetailBottomSheet
import com.tcgvault.app.ui.components.FavoriteStarButton
import com.tcgvault.app.ui.components.GameBadge
import com.tcgvault.app.ui.components.QuantityStepButton
import com.tcgvault.app.ui.components.VaultSearchBar
import com.tcgvault.app.ui.components.gameAccentColor
import com.tcgvault.app.ui.components.rarityColor
import com.tcgvault.app.ui.theme.SubtleBorder
import com.tcgvault.app.ui.theme.TextPrimary
import com.tcgvault.app.ui.theme.TextSecondary
import com.tcgvault.app.ui.theme.VaultEmerald
import com.tcgvault.app.ui.theme.VaultGold
import com.tcgvault.app.ui.theme.VaultSurface
import com.tcgvault.app.ui.theme.VaultSurfaceElevated
import com.tcgvault.app.ui.viewmodel.CardOwnershipFilter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetDetailScreen(
    setProgress: SetWithProgress?,
    cards: List<CardWithCollection>,
    query: String,
    ownershipFilter: CardOwnershipFilter,
    isLoadingCards: Boolean,
    loadError: String?,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (CardOwnershipFilter) -> Unit,
    onCardQuickAdd: (String) -> Unit,
    onAdjustQuantity: (String, Int) -> Unit,
    onToggleCardFavorite: (String) -> Unit
) {
    var detailCardId by remember { mutableStateOf<String?>(null) }
    val activeDetailCard = cards.firstOrNull { it.card.id == detailCardId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = setProgress?.set?.name ?: "Loading Set…",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (setProgress != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameBadge(game = setProgress.set.game)
                        Text(
                            text = "${setProgress.set.code} • ${setProgress.collectedUniqueCount}/${setProgress.displayTotalCards} collected",
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultEmerald
                        )
                    }
                }
            }
        }

        if (setProgress != null) {
            val animatedHeaderProgress by animateFloatAsState(
                targetValue = setProgress.progressFraction,
                animationSpec = tween(420),
                label = "setDetailProgress"
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedHeaderProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = gameAccentColor(setProgress.set.game),
                trackColor = VaultSurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        VaultSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Search cards in set..."
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CardOwnershipFilter.entries.forEach { filter ->
                    val selected = filter == ownershipFilter
                    FilterChip(
                        selected = selected,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VaultGold.copy(alpha = 0.22f),
                            selectedLabelColor = VaultGold,
                            containerColor = VaultSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = SubtleBorder,
                            selectedBorderColor = VaultGold
                        )
                    )
                }
            }
            Text(
                text = "Tap card = +1",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        when {
            isLoadingCards && cards.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = VaultGold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Fetching set cards into local storage…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            loadError != null && cards.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = loadError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            cards.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No cards match this filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(cards, key = { it.card.id }) { item ->
                        SetCardTile(
                            item = item,
                            onTapAdd = { onCardQuickAdd(item.card.id) },
                            onAdjust = { delta -> onAdjustQuantity(item.card.id, delta) },
                            onInspect = { detailCardId = item.card.id },
                            onToggleFavorite = { onToggleCardFavorite(item.card.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }

    if (activeDetailCard != null) {
        CardDetailBottomSheet(
            item = activeDetailCard,
            onDismiss = { detailCardId = null },
            onAdjustQuantity = { delta -> onAdjustQuantity(activeDetailCard.card.id, delta) },
            onToggleFavorite = { onToggleCardFavorite(activeDetailCard.card.id) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SetCardTile(
    item: CardWithCollection,
    onTapAdd: () -> Unit,
    onAdjust: (Int) -> Unit,
    onInspect: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = item.card
    val collected = item.isCollected
    val cardBorderColor by animateColorAsState(
        targetValue = when {
            card.isFavorite -> VaultGold
            collected -> VaultEmerald
            else -> SubtleBorder
        },
        animationSpec = tween(250),
        label = "tileBorder"
    )
    val artworkAlpha by animateFloatAsState(
        targetValue = if (collected) 1f else 0.72f,
        animationSpec = tween(250),
        label = "artAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onTapAdd,
                onLongClick = onInspect
            ),
        color = VaultSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (collected || card.isFavorite) 1.5.dp else 1.dp,
            color = cardBorderColor
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VaultSurfaceElevated)
                    .alpha(artworkAlpha),
                contentAlignment = Alignment.Center
            ) {
                if (!card.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = card.imageUrl,
                        contentDescription = card.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        GameBadge(game = card.game)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (card.manaOrStats.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.manaOrStats,
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Collected badge top-right with pop-in animation
                androidx.compose.animation.AnimatedVisibility(
                    visible = collected,
                    enter = scaleIn(tween(180)) + fadeIn(tween(180)),
                    exit = scaleOut(tween(140)) + fadeOut(tween(140)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Surface(
                        color = VaultEmerald,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Collected",
                                tint = Color(0xFF042617),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "x${item.ownedQuantity}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF042617)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${card.collectorNumber} •",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Text(
                            text = card.rarity,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = rarityColor(card.game, card.rarity),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FavoriteStarButton(
                        isFavorite = card.isFavorite,
                        onClick = onToggleFavorite,
                        sizeDp = 26
                    )
                    Surface(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onInspect),
                        shape = CircleShape,
                        color = VaultSurfaceElevated,
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Card Details",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VaultSurfaceElevated.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuantityStepButton(
                    icon = Icons.Default.Remove,
                    contentDescription = "Remove one",
                    onClick = { onAdjust(-1) },
                    enabled = item.ownedQuantity > 0,
                    isPrimary = false,
                    sizeDp = 30
                )
                AnimatedCounterText(
                    count = item.ownedQuantity,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (collected) VaultEmerald else TextSecondary,
                    suffix = " owned"
                )
                QuantityStepButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add one",
                    onClick = { onAdjust(1) },
                    enabled = true,
                    isPrimary = true,
                    sizeDp = 30
                )
            }
        }
    }
}
