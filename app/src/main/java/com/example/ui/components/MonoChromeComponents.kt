package com.example.ui.components

import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.UtilityTool
import com.example.ui.theme.MonoTheme

@Composable
fun KeepScreenOnEffect(enabled: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current
    DisposableEffect(enabled, view) {
        val window = (context as? android.app.Activity)?.window
        if (enabled) {
            view.keepScreenOn = true
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            view.keepScreenOn = false
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
fun MonoBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = MonoTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bgMain)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle ambient top-right radial lighting
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (palette.isDark) Color(0x24FFFFFF) else Color(0x14000000),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.88f, h * 0.08f),
                    radius = w * 0.78f,
                ),
                radius = w * 0.78f,
                center = Offset(w * 0.88f, h * 0.08f),
            )

            // Large abstract curved Mono geometric arc in background
            val arcStroke = if (palette.isDark) Color(0x0DFFFFFF) else Color(0x0B000000)
            drawArc(
                color = arcStroke,
                startAngle = 150f,
                sweepAngle = 210f,
                useCenter = false,
                topLeft = Offset(w * 0.25f, -h * 0.06f),
                size = Size(w * 1.15f, w * 1.15f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round),
            )
            drawArc(
                color = if (palette.isDark) Color(0x07FFFFFF) else Color(0x06000000),
                startAngle = -35f,
                sweepAngle = 190f,
                useCenter = false,
                topLeft = Offset(-w * 0.45f, h * 0.56f),
                size = Size(w * 1.1f, w * 1.1f),
                style = Stroke(width = 2.0f, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Composable
fun MonoCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    cornerRadius: Dp = 18.dp,
    borderColor: Color = MonoTheme.palette.border,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = MonoTheme.palette
    val topColor = if (elevated) palette.cardElevated else palette.cardPrimary
    val bottomColor = if (elevated) palette.cardPrimary else palette.bgSecondary
    val shape = RoundedCornerShape(cornerRadius)

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = androidx.compose.material3.ripple(color = palette.accent),
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor),
                ),
                shape = shape,
            )
            .border(width = 1.dp, color = borderColor, shape = shape)
            .then(clickModifier)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun MonoSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val palette = MonoTheme.palette
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = palette.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun MonoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val palette = MonoTheme.palette
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.accent,
            contentColor = palette.onAccent,
            disabledContainerColor = palette.surfaceSecondary,
            disabledContentColor = palette.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun MonoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val palette = MonoTheme.palette
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, palette.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.cardPrimary,
            contentColor = palette.textPrimary,
            disabledContentColor = palette.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun MonoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val bg = if (selected) palette.accent else palette.cardPrimary
    val fg = if (selected) palette.onAccent else palette.textSecondary
    val border = if (selected) palette.accent else palette.border

    Surface(
        onClick = onClick,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = fg,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun MonoToolTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    isFavorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val palette = MonoTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("tool_back_button"),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Navigate back",
                tint = palette.textPrimary,
            )
        }
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        actions()
        if (isFavorite != null && onToggleFavorite != null) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("tool_favorite_toggle"),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) palette.accent else palette.textSecondary,
                )
            }
        }
    }
}

@Composable
fun UtilityListCard(
    tool: UtilityTool,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    MonoCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("utility_card_${tool.id}"),
        onClick = onOpen,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceSecondary)
                    .border(1.dp, palette.border, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = palette.textPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.subCategory.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tool.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("fav_btn_${tool.id}"),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Unfavorite ${tool.title}" else "Favorite ${tool.title}",
                    tint = if (isFavorite) palette.accent else palette.textMuted,
                )
            }
        }
    }
}

@Composable
fun MonoEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val palette = MonoTheme.palette
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(palette.cardPrimary)
                .border(1.dp, palette.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.textSecondary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            MonoSecondaryButton(text = actionText, onClick = onAction)
        }
    }
}

data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun MonoBottomNavBar(
    tabs: List<NavTabItem>,
    currentRoute: String?,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = palette.bgSecondary,
        border = BorderStroke(1.dp, palette.border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTab(tab.route) }
                        .padding(vertical = 6.dp)
                        .testTag("nav_tab_${tab.route}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) palette.surfaceSecondary else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = if (selected) palette.textPrimary else palette.textMuted,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) palette.textPrimary else palette.textMuted,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
