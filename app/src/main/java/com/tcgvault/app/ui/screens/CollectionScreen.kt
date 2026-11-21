package com.tcgvault.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tcgvault.app.data.local.model.CardWithCollection
import com.tcgvault.app.data.local.model.CollectionStats
import com.tcgvault.app.ui.components.AnimatedCounterText
import com.tcgvault.app.ui.components.CardDetailBottomSheet
import com.tcgvault.app.ui.components.FavoriteStarButton
import com.tcgvault.app.ui.components.GameBadge
import com.tcgvault.app.ui.components.QuantityStepButton
import com.tcgvault.app.ui.components.VaultSearchBar
import com.tcgvault.app.ui.components.rarityColor
import com.tcgvault.app.ui.theme.MtgAccent
import com.tcgvault.app.ui.theme.SubtleBorder
import com.tcgvault.app.ui.theme.TextPrimary
import com.tcgvault.app.ui.theme.TextSecondary
import com.tcgvault.app.ui.theme.VaultEmerald
import com.tcgvault.app.ui.theme.VaultGold
import com.tcgvault.app.ui.theme.VaultSurface
import com.tcgvault.app.ui.theme.VaultSurfaceElevated
import com.tcgvault.app.ui.theme.YugiohAccent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionScreen(
    stats: CollectionStats,
    ownedCards: List<CardWithCollection>,
    query: String,
    onQueryChange: (String) -> Unit,
    onAdjustQuantity: (String, Int) -> Unit,
    onToggleCardFavorite: (String) -> Unit
) {
    var selectedCardId by remember { mutableStateOf<String?>(null) }
    val activeDetail = ownedCards.firstOrNull { it.card.id == selectedCardId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "My Collection",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary
        )
        Text(
            text = "100% local on-device storage • No backend required",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))
        val animTotal by animateIntAsState(stats.totalCopies, tween(400), label = "statTotal")
        val animYgo by animateIntAsState(stats.yugiohCopies, tween(400), label = "statYgo")
        val animMtg by animateIntAsState(stats.mtgCopies, tween(400), label = "statMtg")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatTile(
                title = "Total Cards",
                value = "$animTotal",
                subtitle = "${stats.uniqueCards} unique",
                accent = VaultGold,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                title = "Yu-Gi-Oh!",
                value = "$animYgo",
                subtitle = "cards",
                accent = YugiohAccent,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                title = "Magic",
                value = "$animMtg",
                subtitle = "${stats.ownedPrecons} precons",
                accent = MtgAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        VaultSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Search collected cards or sets..."
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (ownedCards.isEmpty()) {
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
                        text = "Your vault is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Open any set in the Sets tab and tap a card to add it, or add a Structure Deck / Commander Precon in the Precons tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(ownedCards, key = { it.card.id }) { item ->
                    OwnedCardRow(
                        item = item,
                        onClick = { selectedCardId = item.card.id },
                        onAdjust = { delta -> onAdjustQuantity(item.card.id, delta) },
                        onToggleFavorite = { onToggleCardFavorite(item.card.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }

    if (activeDetail != null) {
        CardDetailBottomSheet(
            item = activeDetail,
            onDismiss = { selectedCardId = null },
            onAdjustQuantity = { delta -> onAdjustQuantity(activeDetail.card.id, delta) },
            onToggleFavorite = { onToggleCardFavorite(activeDetail.card.id) }
        )
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = VaultSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OwnedCardRow(
    item: CardWithCollection,
    onClick: () -> Unit,
    onAdjust: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = item.card
    val rowBorder by animateColorAsState(
        targetValue = if (card.isFavorite) VaultGold.copy(alpha = 0.75f) else SubtleBorder,
        animationSpec = tween(240),
        label = "ownedBorder"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = VaultSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (card.isFavorite) 1.5.dp else 1.dp,
            color = rowBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VaultSurfaceElevated),
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
                    GameBadge(game = card.game)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameBadge(game = card.game)
                    Text(
                        text = "${item.setCode} • ${card.collectorNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = card.rarity,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = rarityColor(card.game, card.rarity),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FavoriteStarButton(
                    isFavorite = card.isFavorite,
                    onClick = onToggleFavorite,
                    sizeDp = 28
                )
                QuantityStepButton(
                    icon = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    onClick = { onAdjust(-1) },
                    enabled = item.ownedQuantity > 0,
                    isPrimary = false,
                    sizeDp = 30
                )
                AnimatedCounterText(
                    count = item.ownedQuantity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VaultEmerald
                )
                QuantityStepButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Increase",
                    onClick = { onAdjust(1) },
                    enabled = true,
                    isPrimary = true,
                    sizeDp = 30
                )
            }
        }
    }
}
