package com.cronoszip.app.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cronoszip.app.data.ExtractionState
import com.cronoszip.app.ui.components.GlassCard
import com.cronoszip.app.ui.components.PrimaryButton
import com.cronoszip.app.ui.components.ProgressRing
import com.cronoszip.app.ui.components.ScreenHeader
import com.cronoszip.app.ui.components.SecondaryButton
import com.cronoszip.app.ui.components.SectionLabel
import com.cronoszip.app.ui.components.StatTile
import com.cronoszip.app.ui.components.StatusPill
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
import com.cronoszip.app.util.formatDuration
import com.cronoszip.app.util.formatPercent
import com.cronoszip.app.util.formatRatio
import com.cronoszip.app.util.formatSpeed
import com.cronoszip.app.util.fileName
import com.cronoszip.app.util.prettyFormat
import com.cronoszip.app.viewmodel.CronosViewModel

@Composable
fun ExtractingScreen(
    viewModel: CronosViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.extraction.collectAsState()
    val check = viewModel.currentCheck
    var confirmPause by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity

    DisposableEffect(state.running) {
        if (state.running) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler {
        if (state.running) {
            confirmPause = true
        } else {
            onBack()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = when {
                    state.success -> "Extraction complete"
                    state.paused -> "Extraction paused"
                    state.errorMessage != null -> "Extraction stopped"
                    else -> "Extracting"
                },
                subtitle = check?.displayName ?: fileName(state.output),
                onBack = { if (state.running) confirmPause = true else onBack() },
            )
        }

        item {
            GlassCard(
                glow = when {
                    state.success -> Success
                    state.errorMessage != null -> Danger
                    state.paused -> Warning
                    else -> CronosCyan
                },
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProgressRing(
                        progress = if (state.success) 1f else state.fraction,
                        modifier = Modifier.size(218.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (state.total > 0) {
                                    formatPercent(if (state.success) 1f else state.fraction)
                                } else {
                                    "…"
                                },
                                style = MaterialTheme.typography.displayMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = phaseLabel(state),
                                style = MaterialTheme.typography.labelMedium,
                                color = phaseColor(state),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = state.message.ifBlank { "Working" },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    if (state.currentFile != null) {
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = state.currentFile ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile(
                            label = "Done",
                            value = formatRatio(state.done, state.total),
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.SpaceDashboard,
                        )
                        StatTile(
                            label = "Speed",
                            value = formatSpeed(state.speed),
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Speed,
                            valueColor = CronosCyan,
                        )
                        StatTile(
                            label = "Left",
                            value = eta(state),
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Timer,
                            valueColor = Warning,
                        )
                    }
                }
            }
        }

        if (state.success) {
            item {
                GlassCard(glow = Success, contentPadding = PaddingValues(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(26.dp),
                        )
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text(
                                text = "Files are in place",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = "${formatCount(state.summary?.itemCount ?: 0)} entries · " +
                                    if (state.summary?.kept == true) {
                                        "The archive was kept."
                                    } else {
                                        "The archive was removed."
                                    },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = state.output,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (state.errorMessage != null) {
            item {
                GlassCard(glow = Danger, contentPadding = PaddingValues(18.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = Danger,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (state.errorCode) {
                                    "NO_SPACE" -> "Not enough space"
                                    "DAMAGED" -> "The archive is damaged"
                                    "WRONG_PASSWORD" -> "Wrong password"
                                    else -> "The extraction stopped"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = state.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                            if (state.errorCode == "NO_SPACE") {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Free up space, then resume. The journal holds the " +
                                        "exact place to continue from.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Warning,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionLabel(text = "What is happening")
        }

        item {
            StagesCard(state = state, verifyFirst = viewModel.options.verifyFirst)
        }

        item {
            GlassCard(contentPadding = PaddingValues(18.dp)) {
                Text(
                    text = "Archive",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(10.dp))
                DetailRow(
                    label = "File",
                    value = check?.displayName ?: fileName(state.output),
                )
                DetailRow(
                    label = "Format",
                    value = check?.format?.let { prettyFormat(it) } ?: "--",
                )
                DetailRow(
                    label = "Mode",
                    value = if (state.keepArchive) "Keep archive" else "Shrink in place",
                )
                DetailRow(
                    label = "Output",
                    value = state.output,
                )
                Spacer(Modifier.height(10.dp))
                val archiveNow = if (state.success && !state.keepArchive) {
                    0L
                } else if (state.committed > 0) {
                    state.committed
                } else {
                    state.archiveSize
                }
                val shrink = when {
                    state.keepArchive -> 0f
                    state.success -> 1f
                    state.archiveSize <= 0 -> 0f
                    state.committed in 1 until state.archiveSize ->
                        1f - (state.committed.toFloat() / state.archiveSize.toFloat())
                    else -> 0f
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Archive space released",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = if (state.keepArchive) {
                            "Kept whole"
                        } else {
                            "${formatPercent(shrink)} · ${formatBytes(archiveNow)} left"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.keepArchive) Warning else Success,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
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
                            .fillMaxWidth(shrink.coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(listOf(CronosBlue, CronosCyan)),
                            ),
                    )
                }
            }
        }

        if (state.logs.isNotEmpty()) {
            item {
                SectionLabel(text = "Activity", trailing = "${state.logs.size} lines")
            }
            item {
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.logs.takeLast(8).forEach { line ->
                            Row {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(CronosCyan.copy(alpha = 0.7f)),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            when {
                state.success -> {
                    PrimaryButton(
                        text = "Done",
                        icon = Icons.Rounded.CheckCircle,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDone,
                    )
                }
                state.running -> {
                    PrimaryButton(
                        text = if (state.cancelling) "Pausing..." else "Pause safely",
                        icon = Icons.Rounded.Pause,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.cancelling,
                        onClick = {
                            viewModel.cancelExtraction()
                        },
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Pausing finishes the files in the current batch before saving " +
                            "progress. No partial file is ever cut off.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.72f),
                    )
                }
                state.paused -> {
                    PrimaryButton(
                        text = "Resume extraction",
                        icon = Icons.Rounded.PlayArrow,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.resumeExtraction() },
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton(
                            text = "Restore archive",
                            icon = Icons.Rounded.Restore,
                            modifier = Modifier.weight(1f),
                            onClick = { confirmRestore = true },
                        )
                        SecondaryButton(
                            text = "Back",
                            modifier = Modifier.weight(1f),
                            onClick = onBack,
                        )
                    }
                }
                else -> {
                    val canResume = state.errorCode in listOf("NO_SPACE", "FAILED", "BUSY")
                    PrimaryButton(
                        text = if (canResume) "Resume extraction" else "Back to plan",
                        icon = if (canResume) Icons.Rounded.PlayArrow else Icons.Rounded.Folder,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (canResume) viewModel.resumeExtraction() else onBack()
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton(
                            text = "Restore archive",
                            icon = Icons.Rounded.Restore,
                            modifier = Modifier.weight(1f),
                            onClick = { confirmRestore = true },
                        )
                        SecondaryButton(
                            text = "Home",
                            modifier = Modifier.weight(1f),
                            onClick = onDone,
                        )
                    }
                }
            }
        }
    }

    if (confirmPause) {
        Dialog(onDismissRequest = { confirmPause = false }) {
            GlassCard(glow = Warning) {
                Text(
                    text = "Pause extraction?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "The current batch finishes safely and progress is saved. You can " +
                        "resume from the same place later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(
                        text = "Keep going",
                        modifier = Modifier.weight(1f),
                        onClick = { confirmPause = false },
                    )
                    PrimaryButton(
                        text = "Pause",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            confirmPause = false
                            viewModel.cancelExtraction()
                        },
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
                        "already extracted stay where they are.",
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
private fun StagesCard(state: ExtractionState, verifyFirst: Boolean) {
    val extracting = state.running && state.phase == "extracting"
    val completed = state.success
    GlassCard(contentPadding = PaddingValues(18.dp)) {
        StageRow(
            title = "Space and safety check",
            detail = "Paths, collisions and free space are proven before anything changes.",
            status = when {
                completed || state.done > 0 || state.phase == "extracting" -> StageStatus.Done
                state.running -> StageStatus.Active
                else -> StageStatus.Pending
            },
        )
        StageRow(
            title = "Verify archive",
            detail = if (verifyFirst) {
                "Every checksum is read before the archive is touched."
            } else {
                "Skipped; checksums are still checked as each file lands."
            },
            status = when {
                !verifyFirst -> StageStatus.Skipped
                completed || (state.phase == "extracting") -> StageStatus.Done
                state.phase == "verifying" -> StageStatus.Active
                else -> StageStatus.Pending
            },
        )
        StageRow(
            title = "Extract entries",
            detail = "Files are written from the end of the archive and flushed to disk.",
            status = when {
                completed -> StageStatus.Done
                extracting -> StageStatus.Active
                state.paused -> StageStatus.Done
                state.errorMessage != null -> StageStatus.Pending
                else -> StageStatus.Pending
            },
        )
        StageRow(
            title = "Release archive space",
            detail = "Each finished batch truncates the archive behind it.",
            status = when {
                completed -> StageStatus.Done
                state.keepArchive -> StageStatus.Skipped
                extracting && state.committed > 0 -> StageStatus.Active
                else -> StageStatus.Pending
            },
        )
        StageRow(
            title = "Clean up",
            detail = "The journal and replaced-files folder are removed only at the end.",
            status = when {
                completed -> StageStatus.Done
                state.running -> StageStatus.Pending
                else -> StageStatus.Pending
            },
            last = true,
        )
    }
}

private enum class StageStatus { Done, Active, Pending, Skipped }

@Composable
private fun StageRow(
    title: String,
    detail: String,
    status: StageStatus,
    last: Boolean = false,
) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when (status) {
                            StageStatus.Done -> Success.copy(alpha = 0.18f)
                            StageStatus.Active -> CronosCyan.copy(alpha = 0.18f)
                            StageStatus.Pending -> GlassWhite.copy(alpha = 0.05f)
                            StageStatus.Skipped -> GlassWhite.copy(alpha = 0.04f)
                        },
                    )
                    .border(
                        1.dp,
                        when (status) {
                            StageStatus.Done -> Success.copy(alpha = 0.45f)
                            StageStatus.Active -> CronosCyan.copy(alpha = 0.5f)
                            else -> Color.White.copy(alpha = 0.1f)
                        },
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when (status) {
                    StageStatus.Done -> Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(15.dp),
                    )
                    StageStatus.Active -> CircularProgressIndicator(
                        modifier = Modifier.size(13.dp),
                        color = CronosCyan,
                        strokeWidth = 2.dp,
                    )
                    else -> Text(
                        text = if (status == StageStatus.Skipped) "–" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
            if (!last) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(Color.White.copy(alpha = 0.09f)),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.padding(top = 1.dp, bottom = if (last) 0.dp else 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (status == StageStatus.Pending) TextSecondary else TextPrimary,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(72.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun phaseLabel(state: ExtractionState): String = when {
    state.success -> "COMPLETE"
    state.paused -> "PAUSED"
    state.errorMessage != null -> "STOPPED"
    state.cancelling -> "PAUSING"
    state.phase == "verifying" -> "VERIFYING"
    state.phase == "preparing" -> "PREPARING"
    state.phase == "extracting" -> "EXTRACTING"
    state.phase == "finishing" -> "FINISHING"
    else -> state.phase.uppercase()
}

private fun phaseColor(state: ExtractionState): Color = when {
    state.success -> Success
    state.paused -> Warning
    state.errorMessage != null -> Danger
    state.cancelling -> Warning
    else -> CronosCyan
}

private fun eta(state: ExtractionState): String {
    if (state.total <= 0 || state.done >= state.total || state.speed <= 0.0) return "--"
    return formatDuration(((state.total - state.done) / state.speed).toLong())
}
