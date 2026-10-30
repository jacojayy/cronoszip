package com.cronoszip.app.ui.screens

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cronoszip.app.data.HistoryEntry
import com.cronoszip.app.ui.components.GlassCard
import com.cronoszip.app.ui.components.GlassChip
import com.cronoszip.app.ui.components.PrimaryButton
import com.cronoszip.app.ui.components.SectionLabel
import com.cronoszip.app.ui.components.StatusPill
import com.cronoszip.app.ui.components.Wordmark
import com.cronoszip.app.ui.theme.CronosBlue
import com.cronoszip.app.ui.theme.CronosCyan
import com.cronoszip.app.ui.theme.GlassWhite
import com.cronoszip.app.ui.theme.Success
import com.cronoszip.app.ui.theme.TextPrimary
import com.cronoszip.app.ui.theme.TextSecondary
import com.cronoszip.app.ui.theme.Warning
import com.cronoszip.app.util.formatBytes
import com.cronoszip.app.util.formatTime
import com.cronoszip.app.util.prettyFormat
import com.cronoszip.app.viewmodel.CronosViewModel

@Composable
fun HomeScreen(
    viewModel: CronosViewModel,
    onGetStarted: () -> Unit,
    onOpenHistory: (String) -> Unit,
    onSettings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Wordmark(
                    modifier = Modifier.align(Alignment.Center),
                    textSize = 40.sp,
                    markSize = 42.dp,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GlassWhite.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable(onClick = onSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Extract huge archives. In place.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
                Text(
                    text = "Built for phones that are almost full",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.72f),
                )
            }
        }

        item {
            GlassCard(glow = CronosCyan.copy(alpha = 0.72f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(text = "In-place engine", color = CronosCyan)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "ZIP · 7Z · TAR · GZ · RAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.8f),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Unpack it.\nDon't duplicate it.",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "CronosZip unpacks from the end of the archive and cuts away each " +
                        "finished part, so the free space the archive gives back is reused for " +
                        "the next files.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip("No double space", selected = true)
                    GlassChip("Resumable")
                }
                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text = "Get started",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onGetStarted,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "The space check runs before anything is changed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.8f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Shield,
                    title = "Checks first",
                    body = "Refuses before a byte is touched.",
                    color = Success,
                )
                FeatureTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.SpaceDashboard,
                    title = "Shrinks as it goes",
                    body = "The archive gives space back.",
                    color = CronosBlue,
                )
            }
        }

        item {
            FeatureTile(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.Bolt,
                title = "Stop and resume",
                body = "Pause, crash, or run out of space. Run it again and it continues.",
                color = Warning,
                horizontal = true,
            )
        }

        item {
            SectionLabel(text = "Recent archives", trailing = "Long live local files")
        }

        if (viewModel.history.isEmpty()) {
            item {
                GlassCard(contentPadding = PaddingValues(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Nothing here yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = "Pick an archive to see its plan. Nothing is extracted " +
                                    "until you confirm.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        } else {
            items(viewModel.history.take(6), key = { it.path + it.timestamp }) { entry ->
                HistoryCard(entry = entry, onClick = { onOpenHistory(entry.path) })
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "CronosZip is a port of HatchZip",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.65f),
                )
                Text(
                    text = "MIT OR Apache-2.0 · RAR via UnRAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.45f),
                )
            }
        }
    }
}

@Composable
private fun FeatureTile(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    body: String,
    color: Color,
    horizontal: Boolean = false,
) {
    GlassCard(modifier = modifier, contentPadding = PaddingValues(16.dp), glow = color) {
        if (horizontal) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeatureIcon(icon = icon, color = color)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        } else {
            FeatureIcon(icon = icon, color = color)
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun FeatureIcon(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(color.copy(alpha = 0.24f), color.copy(alpha = 0.06f)),
                ),
            )
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun HistoryCard(entry: HistoryEntry, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        contentPadding = PaddingValues(15.dp),
        glow = CronosBlue.copy(alpha = 0.6f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(CronosBlue.copy(alpha = 0.3f), CronosCyan.copy(alpha = 0.08f)),
                        ),
                    )
                    .border(1.dp, CronosBlue.copy(alpha = 0.3f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Archive,
                    contentDescription = null,
                    tint = CronosCyan,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOf(
                        prettyFormat(entry.format).takeIf { it.isNotBlank() },
                        entry.status,
                        formatTime(entry.timestamp).takeIf { it.isNotBlank() },
                    ).filterNotNull().joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun HomePreviewStats(entries: Int, size: Long) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${entries} entries", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(
            "· ${formatBytes(size)}",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}
