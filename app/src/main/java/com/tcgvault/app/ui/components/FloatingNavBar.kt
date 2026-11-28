package com.tcgvault.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tcgvault.app.ui.theme.SubtleBorder
import com.tcgvault.app.ui.theme.TextMuted
import com.tcgvault.app.ui.theme.VaultSurfaceElevated

data class FloatingNavItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)

/** Height of the pill itself, excluding outer margins and system insets. */
val FloatingNavBarHeight: Dp = 60.dp

/** Space from the bottom of the screen (above the system nav inset) to the pill. */
val FloatingNavBarBottomMargin: Dp = 12.dp

/**
 * Floating, pill-shaped bottom navigation. The selected tab expands to show its
 * label inside a tinted capsule; unselected tabs collapse to just their icon.
 */
@Composable
fun FloatingNavBar(
    items: List<FloatingNavItem>,
    selectedKey: String?,
    accent: Color,
    onSelect: (FloatingNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(FloatingNavBarHeight),
        shape = CircleShape,
        color = VaultSurfaceElevated.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, SubtleBorder),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                FloatingNavTab(
                    item = item,
                    selected = item.key == selectedKey,
                    accent = accent,
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavTab(
    item: FloatingNavItem,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(220),
        label = "navTabBg"
    )
    val tint by animateColorAsState(
        targetValue = if (selected) accent else TextMuted,
        animationSpec = tween(220),
        label = "navTabTint"
    )

    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(180)) + expandHorizontally(
                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ),
                exit = fadeOut(tween(120)) + shrinkHorizontally(tween(180))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = tint,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
