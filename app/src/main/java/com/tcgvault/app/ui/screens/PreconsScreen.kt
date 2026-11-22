package com.tcgvault.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tcgvault.app.data.local.model.PreconCardDetail
import com.tcgvault.app.data.local.model.PreconWithSummary
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.ui.components.FavoriteStarButton
import com.tcgvault.app.ui.components.GameBadge
import com.tcgvault.app.ui.components.rarityColor
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreconDetailBottomSheet(
    current: PreconWithSummary,
    cards: List<PreconCardDetail>,
    isLoadingCards: Boolean,
    isAdding: Boolean,
    onDismiss: () -> Unit,
    onAddPrecon: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VaultSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameBadge(game = current.precon.game)
                Text(
                    text = "${current.precon.category} · ${current.precon.releaseYear}",
                    fontFamily = MonoFont,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }
            Text(
                text = current.precon.name,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                text = current.precon.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Button(
                onClick = onAddPrecon,
                enabled = !isAdding,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VaultAccentWhite,
                    contentColor = VaultBackground
                )
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = VaultBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adding decklist…", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (current.precon.ownedCount > 0) {
                            "Add Another Copy (Owned ×${current.precon.ownedCount})"
                        } else {
                            "Add Precon + Cards to Collection"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(color = SubtleBorder)
            Text(
                text = "DECKLIST · ${cards.sumOf { it.quantityInDeck }} CARDS",
                fontFamily = MonoFont,
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )

            when {
                isLoadingCards && cards.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = VaultAccentWhite, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Loading decklist…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }

                cards.isEmpty() -> {
                    Text(
                        text = "Decklist unavailable offline. Tap Add Precon while online to fetch its cards.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(cards, key = { it.cardId }) { card ->
                            Surface(
                                color = VaultSurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, SubtleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${card.quantityInDeck}× ${card.cardName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = card.rarity,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = rarityColor(current.precon.game, card.rarity),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "· ${card.cardType}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${card.ownedQuantity}",
                                        fontFamily = MonoFont,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (card.ownedQuantity > 0) VaultEmerald else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreconCard(
    item: PreconWithSummary,
    isAdding: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val precon = item.precon
    val owned = precon.ownedCount > 0
    val borderColor by animateColorAsState(
        targetValue = when {
            precon.isFavorite -> VaultGold.copy(alpha = 0.55f)
            owned -> VaultEmerald.copy(alpha = 0.55f)
            else -> SubtleBorder
        },
        animationSpec = tween(220),
        label = "preconBorder"
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
                            text = precon.setCode,
                            fontFamily = MonoFont,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${precon.releaseYear} · ${precon.category}",
                        fontFamily = MonoFont,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (owned) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Owned",
                                tint = VaultEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "×${precon.ownedCount}",
                                fontFamily = MonoFont,
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    FavoriteStarButton(
                        isFavorite = precon.isFavorite,
                        onClick = onToggleFavorite,
                        sizeDp = 24
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = precon.name,
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
                Text(
                    text = if (item.totalCardsInDeck > 0) {
                        "${item.totalCardsInDeck} cards"
                    } else {
                        "Tap to inspect decklist"
                    },
                    fontFamily = MonoFont,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (owned) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onRemove),
                            color = VaultSurfaceElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SubtleBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Remove precon",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !isAdding, onClick = onAdd),
                        color = VaultAccentWhite,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isAdding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    strokeWidth = 1.8.dp,
                                    color = VaultBackground
                                )
                            } else {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = VaultBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (owned) "+1" else "Add",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VaultBackground,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCustomPreconDialog(
    initialGame: TcgGame,
    onDismiss: () -> Unit,
    onSave: (String, TcgGame, String, Int, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf(initialGame) }
    var category by remember {
        mutableStateOf(if (initialGame == TcgGame.YUGIOH) "Structure Deck" else "Commander Precon")
    }
    var yearText by remember { mutableStateOf("2025") }
    var cardLines by remember { mutableStateOf("1 Sol Ring\n1 Arcane Signet\n1 Command Tower") }
    var addImmediately by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VaultSurface,
        shape = RoundedCornerShape(10.dp),
        title = { Text("Add Custom Precon", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TcgGame.entries.forEach { game ->
                        FilterChip(
                            selected = selectedGame == game,
                            onClick = {
                                selectedGame = game
                                category = if (game == TcgGame.YUGIOH) "Structure Deck" else "Commander Precon"
                            },
                            label = { Text(game.displayName) }
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product / Precon Name") },
                    placeholder = { Text("e.g. Foundations Starter Collection") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Year") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(
                    value = cardLines,
                    onValueChange = { cardLines = it },
                    label = { Text("Decklist (one card per line)") },
                    placeholder = { Text("3 Blue-Eyes White Dragon\n1 Sol Ring") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = addImmediately,
                        onCheckedChange = { addImmediately = it }
                    )
                    Text(
                        text = "Add precon & cards to my collection now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = yearText.toIntOrNull() ?: 2025
                    onSave(name, selectedGame, category, year, cardLines, addImmediately)
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VaultAccentWhite,
                    contentColor = VaultBackground
                )
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
