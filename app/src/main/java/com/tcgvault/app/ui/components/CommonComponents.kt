package com.tcgvault.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tcgvault.app.R
import com.tcgvault.app.data.local.model.CardWithCollection
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.ui.theme.MtgAccent
import com.tcgvault.app.ui.theme.SubtleBorder
import com.tcgvault.app.ui.theme.TextPrimary
import com.tcgvault.app.ui.theme.TextSecondary
import com.tcgvault.app.ui.theme.VaultEmerald
import com.tcgvault.app.ui.theme.VaultGold
import com.tcgvault.app.ui.theme.VaultSurface
import com.tcgvault.app.ui.theme.VaultSurfaceElevated
import com.tcgvault.app.ui.theme.YugiohAccent

fun gameAccentColor(game: TcgGame): Color = when (game) {
    TcgGame.YUGIOH -> YugiohAccent
    TcgGame.MTG -> MtgAccent
}

fun rarityColor(game: TcgGame, rarity: String): Color {
    val r = rarity.trim().lowercase()
    return when (game) {
        TcgGame.MTG -> when {
            r.contains("mythic") -> Color(0xFFF06428)     // Mythic Orange-Red
            r.contains("rare") -> Color(0xFFE5B94C)       // Rare Gold
            r.contains("uncommon") -> Color(0xFFA8C2D8)   // Uncommon Silver
            r.contains("special") || r.contains("bonus") || r.contains("timeshifted") -> Color(0xFFB884FF) // Timeshifted Purple
            else -> Color(0xFFC8CEDF)                     // Common Light Gray/White
        }

        TcgGame.YUGIOH -> when {
            r.contains("quarter century") || r.contains("starlight") || r.contains("ghost") -> Color(0xFF7CE8FF)
            r.contains("secret") || r.contains("collector") || r.contains("ultimate") -> Color(0xFFC084FC)
            r.contains("ultra") -> Color(0xFFE5B94C)
            r.contains("super") -> Color(0xFF93C5FD)
            r.contains("rare") -> Color(0xFFC8CEDF)
            else -> TextSecondary
        }
    }
}

@Composable
fun GameBadge(game: TcgGame, modifier: Modifier = Modifier) {
    val accent = gameAccentColor(game)
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.16f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Text(
            text = game.shortName,
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun GameLogo(game: TcgGame, sizeDp: Int = 20, modifier: Modifier = Modifier) {
    val resId = when (game) {
        TcgGame.YUGIOH -> R.drawable.ic_game_yugioh
        TcgGame.MTG -> R.drawable.ic_game_mtg
    }
    Image(
        painter = painterResource(id = resId),
        contentDescription = game.displayName,
        modifier = modifier.size(sizeDp.dp)
    )
}

@Composable
fun FavoriteStarButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    sizeDp: Int = 28,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isFavorite) VaultGold.copy(alpha = 0.18f) else VaultSurfaceElevated,
        animationSpec = tween(220),
        label = "favBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFavorite) VaultGold.copy(alpha = 0.7f) else SubtleBorder,
        animationSpec = tween(220),
        label = "favBorder"
    )
    val tintColor by animateColorAsState(
        targetValue = if (isFavorite) VaultGold else TextSecondary,
        animationSpec = tween(220),
        label = "favTint"
    )
    val starScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favScale"
    )

    Surface(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = if (isFavorite) "Remove favorite" else "Favorite",
                tint = tintColor,
                modifier = Modifier
                    .size((sizeDp * 0.6f).dp)
                    .scale(starScale)
            )
        }
    }
}

@Composable
fun AnimatedCounterText(
    count: Int,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    fontWeight: FontWeight = FontWeight.Bold,
    suffix: String = ""
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { it / 2 } + fadeIn(tween(160))) togetherWith
                    (slideOutVertically { -it / 2 } + fadeOut(tween(120)))
            } else {
                (slideInVertically { -it / 2 } + fadeIn(tween(160))) togetherWith
                    (slideOutVertically { it / 2 } + fadeOut(tween(120)))
            }
        },
        label = "counterRoll"
    ) { value ->
        Text(
            text = "$value$suffix",
            style = style,
            fontWeight = fontWeight,
            color = color
        )
    }
}

@Composable
fun TopLeftGameSelector(
    selected: TcgGame,
    onSelect: (TcgGame) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val activeAccent by animateColorAsState(
        targetValue = gameAccentColor(selected),
        animationSpec = tween(260),
        label = "gameAccent"
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chevronRot"
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true },
            color = VaultSurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.65f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = {
                        (fadeIn(tween(200)) + slideInVertically { it / 3 }) togetherWith
                            (fadeOut(tween(140)) + slideOutVertically { -it / 3 })
                    },
                    label = "selectedGameContent"
                ) { game ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameLogo(game = game, sizeDp = 20)
                        Text(
                            text = game.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select game",
                    tint = activeAccent,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(chevronRotation)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(VaultSurface)
        ) {
            TcgGame.entries.forEach { option ->
                val optionAccent = gameAccentColor(option)
                val isSelected = option == selected
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GameLogo(game = option, sizeDp = 20)
                            Text(
                                text = option.displayName,
                                color = if (isSelected) optionAccent else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = optionAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun VaultSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        maxLines = 1,
        placeholder = {
            Text(
                text = placeholder,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary)
                }
            }
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VaultSurface,
            unfocusedContainerColor = VaultSurface,
            focusedBorderColor = VaultGold,
            unfocusedBorderColor = SubtleBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
fun QuantityStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    sizeDp: Int = 32
) {
    val containerColor = when {
        !enabled -> VaultSurfaceElevated.copy(alpha = 0.45f)
        isPrimary -> VaultGold
        else -> VaultSurfaceElevated
    }
    val contentColor = when {
        !enabled -> TextSecondary.copy(alpha = 0.45f)
        isPrimary -> Color(0xFF1A1404)
        else -> TextPrimary
    }

    Surface(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = containerColor,
        border = if (!isPrimary) BorderStroke(1.dp, SubtleBorder) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size((sizeDp * 0.54f).dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailBottomSheet(
    item: CardWithCollection,
    onDismiss: () -> Unit,
    onAdjustQuantity: (Int) -> Unit,
    onToggleFavorite: () -> Unit
) {
    val card = item.card
    val accent = gameAccentColor(card.game)

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(108.dp)
                        .height(152.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            GameBadge(game = card.game)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = card.collectorNumber,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameBadge(game = card.game)
                            Text(
                                text = card.collectorNumber,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                        FavoriteStarButton(
                            isFavorite = card.isFavorite,
                            onClick = onToggleFavorite,
                            sizeDp = 30
                        )
                    }
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = card.cardType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = accent
                    )
                    if (card.manaOrStats.isNotBlank()) {
                        Text(
                            text = card.manaOrStats,
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultGold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = card.rarity,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = rarityColor(card.game, card.rarity)
                        )
                        if (item.setName.isNotBlank()) {
                            Text(
                                text = "• ${item.setName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (card.description.isNotBlank()) {
                Surface(
                    color = VaultSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Quantity control row
            Surface(
                color = VaultSurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (item.isCollected) VaultEmerald.copy(alpha = 0.6f) else SubtleBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "In Your Collection",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (item.isCollected) "Stored locally on device" else "Tap + to add this card",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.isCollected) VaultEmerald else TextSecondary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuantityStepButton(
                            icon = Icons.Default.Remove,
                            contentDescription = "Decrease quantity",
                            onClick = { onAdjustQuantity(-1) },
                            enabled = item.ownedQuantity > 0,
                            isPrimary = false,
                            sizeDp = 36
                        )
                        Text(
                            text = "${item.ownedQuantity}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (item.isCollected) VaultGold else TextSecondary
                        )
                        QuantityStepButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            onClick = { onAdjustQuantity(1) },
                            enabled = true,
                            isPrimary = true,
                            sizeDp = 36
                        )
                    }
                }
            }
        }
    }
}
