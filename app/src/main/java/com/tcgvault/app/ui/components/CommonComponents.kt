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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tcgvault.app.R
import com.tcgvault.app.data.local.model.CardWithCollection
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.ui.theme.MonoFont
import com.tcgvault.app.ui.theme.MtgAccent
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
            r.contains("special") || r.contains("bonus") || r.contains("timeshifted") -> Color(0xFFB884FF)
            else -> Color(0xFFD4D4D8)                     // Common Light Gray
        }

        TcgGame.YUGIOH -> when {
            r.contains("quarter century") || r.contains("starlight") || r.contains("ghost") -> Color(0xFF7CE8FF)
            r.contains("secret") || r.contains("collector") || r.contains("ultimate") -> Color(0xFFC084FC)
            r.contains("ultra") -> Color(0xFFE5B94C)
            r.contains("super") -> Color(0xFF93C5FD)
            r.contains("rare") -> Color(0xFFD4D4D8)
            else -> TextMuted
        }
    }
}

@Composable
fun GameBadge(game: TcgGame, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = VaultSurfaceElevated,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Text(
            text = game.shortName,
            color = TextSecondary,
            fontFamily = MonoFont,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

@Composable
fun GameLogo(game: TcgGame, sizeDp: Int = 18, modifier: Modifier = Modifier) {
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
    sizeDp: Int = 26,
    modifier: Modifier = Modifier
) {
    val tintColor by animateColorAsState(
        targetValue = if (isFavorite) VaultGold else TextMuted,
        animationSpec = tween(200),
        label = "favTint"
    )
    val starScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favScale"
    )

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
            contentDescription = if (isFavorite) "Remove favorite" else "Favorite",
            tint = tintColor,
            modifier = Modifier
                .size((sizeDp * 0.72f).dp)
                .scale(starScale)
        )
    }
}

@Composable
fun AnimatedCounterText(
    count: Int,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    fontWeight: FontWeight = FontWeight.SemiBold,
    suffix: String = ""
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { it / 2 } + fadeIn(tween(150))) togetherWith
                    (slideOutVertically { -it / 2 } + fadeOut(tween(110)))
            } else {
                (slideInVertically { -it / 2 } + fadeIn(tween(150))) togetherWith
                    (slideOutVertically { it / 2 } + fadeOut(tween(110)))
            }
        },
        label = "counterRoll"
    ) { value ->
        Text(
            text = "$value$suffix",
            style = style,
            fontFamily = MonoFont,
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
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chevronRot"
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true },
            color = VaultSurface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SubtleBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = {
                        (fadeIn(tween(180)) + slideInVertically { it / 3 }) togetherWith
                            (fadeOut(tween(120)) + slideOutVertically { -it / 3 })
                    },
                    label = "selectedGameContent"
                ) { game ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameLogo(game = game, sizeDp = 18)
                        Text(
                            text = game.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select game",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(16.dp)
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
                val isSelected = option == selected
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GameLogo(game = option, sizeDp = 18)
                            Text(
                                text = option.displayName,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
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
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp),
        color = VaultSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(17.dp)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    cursorBrush = SolidColor(VaultAccentWhite),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityStepButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    sizeDp: Int = 28
) {
    val containerColor = when {
        !enabled -> VaultSurfaceElevated.copy(alpha = 0.4f)
        isPrimary -> VaultAccentWhite
        else -> VaultSurfaceElevated
    }
    val contentColor = when {
        !enabled -> TextMuted.copy(alpha = 0.45f)
        isPrimary -> VaultBackground
        else -> TextPrimary
    }

    Surface(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        border = if (!isPrimary) BorderStroke(1.dp, SubtleBorder) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size((sizeDp * 0.56f).dp)
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
                        .width(104.dp)
                        .height(146.dp)
                        .clip(RoundedCornerShape(6.dp))
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
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = card.collectorNumber,
                                fontFamily = MonoFont,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GameBadge(game = card.game)
                            Text(
                                text = "#${card.collectorNumber}",
                                fontFamily = MonoFont,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
                            )
                        }
                        FavoriteStarButton(
                            isFavorite = card.isFavorite,
                            onClick = onToggleFavorite,
                            sizeDp = 28
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
                        color = TextSecondary
                    )
                    if (card.manaOrStats.isNotBlank()) {
                        Text(
                            text = card.manaOrStats,
                            fontFamily = MonoFont,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
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
                                text = "· ${item.setName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (card.description.isNotBlank()) {
                HorizontalDivider(color = SubtleBorder)
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Surface(
                color = VaultSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (item.isCollected) VaultEmerald.copy(alpha = 0.55f) else SubtleBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Owned copies",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (item.isCollected) "Stored locally" else "Not in collection",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.isCollected) VaultEmerald else TextMuted
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
                            sizeDp = 34
                        )
                        AnimatedCounterText(
                            count = item.ownedQuantity,
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (item.isCollected) TextPrimary else TextMuted
                        )
                        QuantityStepButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            onClick = { onAdjustQuantity(1) },
                            enabled = true,
                            isPrimary = true,
                            sizeDp = 34
                        )
                    }
                }
            }
        }
    }
}
