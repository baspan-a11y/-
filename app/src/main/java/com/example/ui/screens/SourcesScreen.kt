package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.NewsCategory
import com.example.data.model.NewsSourceEntity
import com.example.data.remote.PaoClassifier
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ShamrockGreen
import com.example.ui.viewmodel.NewsUiState

@Composable
fun SourcesScreen(
    uiState: NewsUiState,
    onToggleSource: (String, Boolean) -> Unit,
    onRefreshAll: () -> Unit,
    onOpenSourceFeed: (String) -> Unit,
    onAddCustomSource: (String, String, NewsCategory) -> Unit,
    onDeleteCustomSource: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_source_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Προσθήκη προσαρμοσμένης πηγής RSS"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .testTag("sources_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "sources_header") {
                    SourcesSummaryBanner(
                        enabledCount = uiState.sources.count { it.isEnabled },
                        totalCount = uiState.sources.size,
                        isRefreshing = uiState.isRefreshing,
                        onRefreshAll = onRefreshAll
                    )
                }

                items(uiState.sources, key = { it.id }) { source ->
                    SourceCard(
                        source = source,
                        onToggle = { enabled -> onToggleSource(source.id, enabled) },
                        onClick = { onOpenSourceFeed(source.id) },
                        onDelete = if (source.isCustom) {
                            { onDeleteCustomSource(source.id) }
                        } else null
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomSourceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, url, cat ->
                onAddCustomSource(name, url, cat)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SourcesSummaryBanner(
    enabledCount: Int,
    totalCount: Int,
    isRefreshing: Boolean,
    onRefreshAll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(DeepForestGreen, ShamrockGreen)
                    ),
                    shape = MaterialTheme.shapes.large
                )
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.RssFeed,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Πηγές Ενημέρωσης",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$enabledCount από $totalCount πηγές ενεργές. Πατήστε σε οποιαδήποτε πηγή για να φιλτράρετε τη ροή.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onRefreshAll,
                    enabled = !isRefreshing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = DeepForestGreen
                    ),
                    modifier = Modifier.testTag("sync_all_sources_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRefreshing) "Συγχρονισμός…" else "Ενημέρωση όλων")
                }
            }
        }
    }
}

@Composable
private fun SourceCard(
    source: NewsSourceEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("source_card_${source.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(source.accentColorHex))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = source.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (source.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Προσαρμοσμένη",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = source.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                val statusText = when {
                    source.lastError != null -> "Σφάλμα συγχρονισμού"
                    source.lastSyncTime != null ->
                        "${source.lastArticleCount} άρθρα • ${PaoClassifier.formatRelativeTimeEl(source.lastSyncTime)}"
                    else -> "Έτοιμο για συγχρονισμό"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (source.lastError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_source_${source.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Διαγραφή πηγής",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Switch(
                checked = source.isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("source_switch_${source.id}")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCustomSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, NewsCategory) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var feedUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NewsCategory.FOOTBALL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Προσθήκη Πηγής RSS") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Όνομα ιστοσελίδας") },
                    placeholder = { Text("π.χ. Leoforos.gr") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_source_name_input")
                )
                OutlinedTextField(
                    value = feedUrl,
                    onValueChange = { feedUrl = it },
                    label = { Text("Διεύθυνση RSS Feed") },
                    placeholder = { Text("https://example.gr/feed/") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_source_url_input")
                )
                Text(
                    text = "Κύρια κατηγορία",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NewsCategory.entries
                        .filter { it != NewsCategory.ALL }
                        .forEach { cat ->
                            FilterChip(
                                selected = cat == selectedCategory,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.labelEl) }
                            )
                        }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, feedUrl, selectedCategory) },
                enabled = name.isNotBlank() && feedUrl.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_source_button")
            ) {
                Text("Προσθήκη")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ακύρωση")
            }
        }
    )
}
