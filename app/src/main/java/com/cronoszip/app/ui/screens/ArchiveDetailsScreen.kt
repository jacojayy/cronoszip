package com.cronoszip.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cronoszip.app.data.EntryData
import com.cronoszip.app.ui.components.GlassCard
import com.cronoszip.app.ui.components.PrimaryButton
import com.cronoszip.app.ui.components.ScreenHeader
import com.cronoszip.app.ui.components.SecondaryButton
import com.cronoszip.app.ui.components.StatTile
import com.cronoszip.app.ui.theme.CronosBlue
import com.cronoszip.app.ui.theme.CronosCyan
import com.cronoszip.app.ui.theme.Danger
import com.cronoszip.app.ui.theme.GlassWhite
import com.cronoszip.app.ui.theme.Success
import com.cronoszip.app.ui.theme.TextPrimary
import com.cronoszip.app.ui.theme.TextSecondary
import com.cronoszip.app.ui.theme.Warning
import com.cronoszip.app.util.formatBytes
import com.cronoszip.app.util.formatCount
import com.cronoszip.app.util.formatPercent
import com.cronoszip.app.util.prettyFormat
import com.cronoszip.app.viewmodel.CronosViewModel

@Composable
fun ArchiveDetailsScreen(
    viewModel: CronosViewModel,
    onBack: () -> Unit,
) {
    val details by viewModel.details.collectAsState()
    val test by viewModel.test.collectAsState()
    val check = viewModel.currentCheck
    var query by remember { mutableStateOf("") }
    var confirmRestore by remember { mutableStateOf(false) }

    LaunchedEffect(check?.archive) {
        if (check != null) viewModel.openDetails()
    }

    val entries = details.list?.entries.orEmpty()
    val filtered = if (query.isBlank()) {
        entries
    } else {
        entries.filter { it.path.contains(query, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Archive contents",
                subtitle = check?.displayName,
                onBack = onBack,
            )
        }

        item {
            GlassCard(glow = CronosBlue) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = check?.displayName ?: "Archive",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = listOf(
                                check?.format?.let { prettyFormat(it) },
                                "${formatCount(check?.entryCount ?: 0)} entries",
                                formatBytes(check?.archiveSize ?: 0L),
                            ).filterNotNull().joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = null,
                        tint = CronosCyan,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        label = "Uncompressed",
                        value = formatBytes(check?.uncompressedSize ?: 0L),
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "Archive",
                        value = formatBytes(check?.archiveSize ?: 0L),
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "Entries",
                        value = formatCount(check?.entryCount ?: 0),
                        modifier = Modifier.weight(1f),
                        valueColor = CronosCyan,
                    )
                }
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = if (test.running) "Testing..." else "Test every entry",
                    icon = Icons.Rounded.Shield,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !test.running && !details.loading,
                    onClick = { viewModel.testArchive() },
                )
                if (test.running) {
                    Spacer(Modifier.height(10.dp))
                    val fraction = if (test.total > 0) {
                        test.done.toFloat() / test.total.toFloat()
                    } else {
                        0f
                    }
                    Text(
                        text = test.message ?: "Checking checksums",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(GlassWhite.copy(alpha = 0.07f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(CronosBlue, CronosCyan))),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${formatPercent(fraction)} · ${formatBytes(test.done)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
                if (test.passed == true) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = test.message ?: "All checksums pass.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Success,
                        )
                    }
                }
                if (test.passed == false) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = Danger,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = test.error ?: "The test failed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Danger,
                        )
                    }
                }
                if (check?.resuming == true) {
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton(
                        text = "Restore archive",
                        icon = Icons.Rounded.Restore,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { confirmRestore = true },
                    )
                }
            }
        }

        if (details.loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CronosCyan, strokeWidth = 3.dp)
                }
            }
        } else if (details.error != null) {
            item {
                GlassCard(glow = Danger) {
                    Text(
                        text = "Could not list the archive",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = details.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton(
                        text = "Try again",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.refreshDetails() },
                    )
                }
            }
        } else {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search ${formatCount(entries.size)} entries",
                            color = TextSecondary,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                )
            }

            items(filtered, key = { it.path + it.kind }) { entry ->
                EntryRow(entry = entry)
            }

            if (filtered.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = if (entries.isEmpty()) "This archive has no entries." else "No entry matches.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }

            if (details.list?.truncated == true) {
                item {
                    Text(
                        text = "Showing the first 5,000 entries. The extractor still processes all " +
                            "${formatCount(check?.entryCount ?: 0)}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning,
                    )
                }
            }
        }
    }

    if (confirmRestore) {
        Dialog(onDismissRequest = { confirmRestore = false }) {
            GlassCard(glow = Danger) {
                Text(
                    text = "Restore the archive?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Entries not extracted yet become a standard archive again. Files " +
                        "already extracted stay in the destination folder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(
                        text = "Cancel",
                        modifier = Modifier.weight(1f),
                        onClick = { confirmRestore = false },
                    )
                    PrimaryButton(
                        text = "Restore",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            confirmRestore = false
                            viewModel.restoreArchive()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: EntryData) {
    val (icon, color) = when (entry.kind) {
        "dir" -> Icons.Rounded.Folder to CronosCyan
        "symlink", "hardlink" -> Icons.Rounded.Link to Warning
        else -> Icons.Rounded.InsertDriveFile to TextSecondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassWhite.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = entry.path,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (entry.kind == "dir") "" else formatBytes(entry.size),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}
