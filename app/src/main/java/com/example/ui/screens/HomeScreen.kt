package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UtilityCatalog
import com.example.data.model.UtilityCategory
import com.example.data.model.UtilityTool
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPrimaryButton
import com.example.ui.components.MonoSectionHeader
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    favoriteTools: List<UtilityTool>,
    recentTools: List<UtilityTool>,
    onOpenTool: (String) -> Unit,
    onOpenCategory: (UtilityCategory) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onSeeAllExplore: () -> Unit,
    onSeeAllFavorites: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val featured = UtilityCatalog.featuredTools
    val quickTools = UtilityCatalog.quickTools
    val pagerState = rememberPagerState(pageCount = { featured.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(5_500L)
            if (featured.isNotEmpty() && !pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % featured.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello there!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.textSecondary,
                        )
                        Text(
                            text = "Welcome to Mono PlayKit",
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onOpenSearch,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(palette.cardPrimary)
                                .border(1.dp, palette.border, CircleShape)
                                .testTag("home_search_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search utilities",
                                tint = palette.textPrimary,
                            )
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(palette.cardPrimary)
                                .border(1.dp, palette.border, CircleShape)
                                .testTag("home_settings_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Open settings",
                                tint = palette.textPrimary,
                            )
                        }
                    }
                }
            }

            // Rotating Visual Featured Utility Card
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 12.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        val tool = featured[page]
                        FeaturedVisualCard(
                            tool = tool,
                            onClick = { onOpenTool(tool.id) },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(featured.size) { idx ->
                            val active = pagerState.currentPage == idx
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(5.dp)
                                    .width(if (active) 22.dp else 6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (active) palette.accent else palette.border)
                            )
                        }
                    }
                }
            }

            // Mobile-friendly 2-column Quick Utilities Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MonoSectionHeader(
                        title = "Quick Utilities",
                        actionLabel = "All tools",
                        onActionClick = onSeeAllExplore,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        quickTools.chunked(2).forEach { rowTools ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowTools.forEach { tool ->
                                    QuickUtilityTile(
                                        tool = tool,
                                        onClick = { onOpenTool(tool.id) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (rowTools.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Pinned Favorites
            if (favoriteTools.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MonoSectionHeader(
                            title = "Pinned Favorites",
                            actionLabel = "Manage",
                            onActionClick = onSeeAllFavorites,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(favoriteTools, key = { it.id }) { tool ->
                                CompactToolChipCard(
                                    tool = tool,
                                    onClick = { onOpenTool(tool.id) },
                                )
                            }
                        }
                    }
                }
            }

            // Recently Used
            if (recentTools.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MonoSectionHeader(title = "Recently Used")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentTools, key = { "recent_${it.id}" }) { tool ->
                                CompactToolChipCard(
                                    tool = tool,
                                    onClick = { onOpenTool(tool.id) },
                                )
                            }
                        }
                    }
                }
            }

            // Visual 2-Column Category Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MonoSectionHeader(
                        title = "Browse Categories",
                        actionLabel = "Explore",
                        onActionClick = onSeeAllExplore,
                    )
                    val categories = UtilityCategory.entries
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categories.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                pair.forEach { cat ->
                                    CategoryVisualCard(
                                        category = cat,
                                        toolCount = UtilityCatalog.byCategory(cat).size,
                                        onClick = { onOpenCategory(cat) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(Modifier.weight(1f))
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
private fun FeaturedVisualCard(
    tool: UtilityTool,
    onClick: () -> Unit,
) {
    val palette = MonoTheme.palette
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(shape)
            .border(1.dp, palette.border, shape)
            .clickable(onClick = onClick)
            .testTag("featured_card_${tool.id}"),
    ) {
        Image(
            painter = painterResource(id = tool.visualResId),
            contentDescription = tool.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // High-contrast dark left-to-right & bottom scrim so white text is always crisp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0.0f to Color(0xF0080808),
                        0.58f to Color(0xC8080808),
                        1.0f to Color(0x55080808),
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x22080808),
                        1.0f to Color(0xD8080808),
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xB3191919))
                    .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "FEATURED • ${tool.category.title.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE8E8E8),
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tool.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD0D0D0),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(12.dp))
                MonoPrimaryButton(
                    text = "Open ${tool.shortTitle}",
                    onClick = onClick,
                    icon = Icons.Outlined.PlayArrow,
                )
            }
        }
    }
}

@Composable
private fun QuickUtilityTile(
    tool: UtilityTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    MonoCard(
        modifier = modifier
            .height(82.dp)
            .testTag("quick_tile_${tool.id}"),
        onClick = onClick,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceSecondary)
                    .border(1.dp, palette.border, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.shortTitle,
                    tint = palette.textPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.shortTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = tool.subCategory,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CategoryVisualCard(
    category: UtilityCategory,
    toolCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .height(114.dp)
            .clip(shape)
            .border(1.dp, palette.border, shape)
            .clickable(onClick = onClick)
            .testTag("home_category_${category.id}"),
    ) {
        Image(
            painter = painterResource(id = category.bannerResId),
            contentDescription = category.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xBB0D0D0D),
                        1.0f to Color(0xEC080808),
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x99222222))
                        .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x99222222))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "$toolCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE0E0E0),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Column {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBDBDBD),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CompactToolChipCard(
    tool: UtilityTool,
    onClick: () -> Unit,
) {
    val palette = MonoTheme.palette
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.cardPrimary)
            .border(1.dp, palette.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = tool.icon,
            contentDescription = tool.shortTitle,
            tint = palette.accent,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = tool.shortTitle,
            style = MaterialTheme.typography.labelLarge,
            color = palette.textPrimary,
            maxLines = 1,
        )
    }
}
