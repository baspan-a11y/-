package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ArticleEntity
import com.example.data.model.NewsCategory
import com.example.data.remote.PaoClassifier
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ShamrockGreen

@Composable
fun ShamrockCrestBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = ShamrockGreen
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.48f
            val r = w * 0.21f
            // Top leaf
            drawCircle(color = Color.White, radius = r, center = Offset(cx, cy - r * 1.15f))
            // Left leaf
            drawCircle(color = Color.White, radius = r, center = Offset(cx - r * 1.1f, cy + r * 0.35f))
            // Right leaf
            drawCircle(color = Color.White, radius = r, center = Offset(cx + r * 1.1f, cy + r * 0.35f))
            // Center join & gold stem dot
            drawCircle(color = Color.White, radius = r * 0.65f, center = Offset(cx, cy))
            drawLine(
                color = ChampionshipGold,
                start = Offset(cx, cy),
                end = Offset(cx, h * 0.96f),
                strokeWidth = w * 0.12f
            )
        }
    }
}

@Composable
fun EditorialTopHeader(
    articleCount: Int,
    lastUpdatedMillis: Long?,
    isRefreshing: Boolean,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refreshSpin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShamrockCrestBadge()
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Πράσινα Νέα",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = when {
                isRefreshing -> "Συγχρονισμός πηγών…"
                lastUpdatedMillis != null && articleCount > 0 ->
                    "$articleCount ειδήσεις • ${PaoClassifier.formatRelativeTimeEl(lastUpdatedMillis)}"
                else -> "Όλα τα νέα του Παναθηναϊκού"
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onToggleSearch,
            modifier = Modifier.testTag("search_toggle_button")
        ) {
            Icon(
                imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                contentDescription = if (isSearchActive) "Κλείσιμο αναζήτησης" else "Αναζήτηση ειδήσεων",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier.testTag("refresh_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isRefreshing) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        rotate(spinAngle) {
                            drawArc(
                                color = ShamrockGreen,
                                startAngle = 0f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Ανανέωση ειδήσεων",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: NewsCategory,
    categoryCounts: Map<NewsCategory, Int>,
    onSelectCategory: (NewsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(NewsCategory.entries, key = { it.name }) { category ->
            val selected = category == selectedCategory
            val count = categoryCounts[category] ?: 0
            val label = if (count > 0 && category != NewsCategory.ALL) {
                "${category.labelEl} ($count)"
            } else {
                category.labelEl
            }
            FilterChip(
                selected = selected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
            )
        }
    }
}

@Composable
fun StatusBanner(
    message: String?,
    isError: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isError) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isError) {
                    Icon(
                        imageVector = Icons.Outlined.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = message.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Απόρριψη μηνύματος",
                        modifier = Modifier.size(16.dp),
                        tint = if (isError) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedArticleCard(
    article: ArticleEntity,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = NewsCategory.fromKey(article.category)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .testTag("featured_article_card"),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
        ) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                placeholder = painterResource(id = R.drawable.img_hero_stadium),
                error = painterResource(id = R.drawable.img_hero_stadium),
                fallback = painterResource(id = R.drawable.img_hero_stadium),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.20f),
                                Color.Black.copy(alpha = 0.35f),
                                Color(0xFF041C0F).copy(alpha = 0.94f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = ChampionshipGold,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ΚΟΡΥΦΑΙΟ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1F1600),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = ShamrockGreen.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = category.labelEl,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.testTag("featured_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (article.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (article.isSaved) "Αφαίρεση από αποθηκευμένα" else "Αποθήκευση άρθρου",
                            tint = if (article.isSaved) ChampionshipGold else Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${article.sourceName} • ${PaoClassifier.formatRelativeTimeEl(article.publishedAt)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ArticleListCard(
    article: ArticleEntity,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = NewsCategory.fromKey(article.category)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("article_card_${article.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = category.labelEl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${article.sourceName} • ${PaoClassifier.formatRelativeTimeEl(article.publishedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (!article.isRead) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (article.isRead) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (article.summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.title,
                    placeholder = painterResource(id = R.drawable.img_hero_stadium),
                    error = painterResource(id = R.drawable.img_hero_stadium),
                    fallback = painterResource(id = R.drawable.img_hero_stadium),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 96.dp, height = 84.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("share_button_${article.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Κοινοποίηση άρθρου",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.testTag("bookmark_button_${article.id}")
                ) {
                    Icon(
                        imageVector = if (article.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (article.isSaved) "Αφαίρεση από αποθηκευμένα" else "Αποθήκευση άρθρου",
                        tint = if (article.isSaved) ChampionshipGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}
