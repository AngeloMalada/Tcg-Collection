package com.tcgvault.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tcgvault.app.data.local.model.PreconCardDetail
import com.tcgvault.app.data.local.model.PreconWithSummary
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.ui.components.FavoriteStarButton
import com.tcgvault.app.ui.components.GameBadge
import com.tcgvault.app.ui.components.rarityColor
import com.tcgvault.app.ui.theme.SubtleBorder
import com.tcgvault.app.ui.theme.TextPrimary
import com.tcgvault.app.ui.theme.TextSecondary
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
                    text = "${current.precon.category} • ${current.precon.releaseYear}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VaultGold,
                    contentColor = Color(0xFF1A1404)
                )
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF1A1404)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adding Decklist…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (current.precon.ownedCount > 0) {
                            "Add Another Copy (Owned x${current.precon.ownedCount})"
                        } else {
                            "Add Precon + Cards to Collection"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Decklist (${cards.sumOf { it.quantityInDeck }} cards)",
                style = MaterialTheme.typography.titleMedium,
                color = VaultGold
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
                            CircularProgressIndicator(color = VaultGold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Loading decklist into local storage…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                cards.isEmpty() -> {
                    Text(
                        text = "Decklist could not be loaded offline. Connect to the internet and tap Add Precon to fetch its cards.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${card.quantityInDeck}x ${card.cardName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = card.rarity,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = rarityColor(current.precon.game, card.rarity),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "• ${card.cardType}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Own ${card.ownedQuantity}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (card.ownedQuantity > 0) VaultEmerald else TextSecondary
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
            precon.isFavorite -> VaultGold.copy(alpha = 0.75f)
            owned -> VaultEmerald
            else -> SubtleBorder
        },
        animationSpec = tween(250),
        label = "preconBorder"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = VaultSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (owned || precon.isFavorite) 1.5.dp else 1.dp,
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
                    GameBadge(game = precon.game)
                    Text(
                        text = "${precon.category} • ${precon.releaseYear}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (owned) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Owned",
                                tint = VaultEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Owned x${precon.ownedCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    FavoriteStarButton(
                        isFavorite = precon.isFavorite,
                        onClick = onToggleFavorite,
                        sizeDp = 26
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = precon.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = precon.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
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
                    text = if (item.totalCardsInDeck > 0) {
                        "${item.totalCardsInDeck} cards tracked"
                    } else {
                        "Set ${precon.setCode}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = VaultGold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (owned) {
                        OutlinedButton(
                            onClick = onRemove,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove precon", modifier = Modifier.size(16.dp))
                        }
                    }
                    Button(
                        onClick = onAdd,
                        enabled = !isAdding,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultGold,
                            contentColor = Color(0xFF1A1404)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF1A1404)
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (owned) "Add +1" else "Add Precon",
                                fontWeight = FontWeight.Bold
                            )
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
        title = { Text("Add Custom Precon", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = VaultGold,
                    contentColor = Color(0xFF1A1404)
                )
            ) {
                Text("Save Precon", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
