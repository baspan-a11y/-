package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ArticleEntity
import com.example.ui.components.ArticleListCard

@Composable
fun SavedScreen(
    savedArticles: List<ArticleEntity>,
    onArticleClick: (ArticleEntity) -> Unit,
    onToggleSave: (ArticleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmarks,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Αποθηκευμένα",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${savedArticles.size} αποθηκευμένα άρθρα για ανάγνωση εκτός σύνδεσης",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (savedArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_empty_saved),
                        contentDescription = "Κενή λίστα αποθηκευμένων άρθρων",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 240.dp, height = 180.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Δεν έχετε αποθηκευμένα άρθρα",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Πατήστε το εικονίδιο σελιδοδείκτη σε οποιοδήποτε νέο του Παναθηναϊκού για να το κρατήσετε εδώ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .testTag("saved_articles_list"),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedArticles, key = { it.id }) { article ->
                    ArticleListCard(
                        article = article,
                        onClick = { onArticleClick(article) },
                        onToggleSave = { onToggleSave(article) },
                        onShare = { shareArticle(context, article) }
                    )
                }
            }
        }
    }
}
