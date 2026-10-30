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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cronoszip.app.data.CheckData
import com.cronoszip.app.data.SetupOptions
import com.cronoszip.app.ui.components.GlassCard
import com.cronoszip.app.ui.components.GlassChip
import com.cronoszip.app.ui.components.PlanBar
import com.cronoszip.app.ui.components.PrimaryButton
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
import com.cronoszip.app.util.prettyFormat
import com.cronoszip.app.viewmodel.CronosViewModel

@Composable
fun ExtractSetupScreen(
    viewModel: CronosViewModel,
    onBack: () -> Unit,
    onChooseFolder: () -> Unit,
    onViewContents: () -> Unit,
) {
    val check = viewModel.currentCheck
    val options = viewModel.options
    var confirmRestore by remember { mutableStateOf(false) }

    if (check == null) {
        Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
            GlassCard(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "No archive is selected",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Go back and choose an archive first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(16.dp))
                SecondaryButton(text = "Back", modifier = Modifier.fillMaxWidth(), onClick = onBack)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "Extraction plan",
                subtitle = check.displayName,
                onBack = onBack,
            )
        }

        item {
            GlassCard(glow = CronosCyan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(CronosBlue.copy(alpha = 0.34f), CronosCyan.copy(alpha = 0.1f)),
                                ),
                            )
                            .border(1.dp, CronosBlue.copy(alpha = 0.35f), RoundedCornerShape(17.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Archive,
                            contentDescription = null,
                            tint = CronosCyan,
                            modifier = Modifier.size(27.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = check.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusPill(text = prettyFormat(check.format), color = CronosCyan)
                            if (check.encrypted) {
                                StatusPill(text = "Encrypted", color = Warning)
                            }
                            if (check.resuming) {
                                StatusPill(text = "Resuming", color = CronosBlue)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        label = "Entries",
                        value = formatCount(check.entryCount),
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.List,
                    )
                    StatTile(
                        label = "Output",
                        value = formatBytes(check.uncompressedSize),
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.SpaceDashboard,
                    )
                    StatTile(
                        label = "Archive",
                        value = formatBytes(check.archiveSize),
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Archive,
                    )
                }
                if (check.resuming) {
                    Spacer(Modifier.height(12.dp))
                    InfoNote(
                        icon = Icons.Rounded.Restore,
                        color = CronosBlue,
                        text = "A partial extraction was found. This run continues from the " +
                            "saved journal, in the mode it was started in.",
                    )
                }
            }
        }

        item {
            SectionLabel(text = "Destination")
        }

        item {
            GlassCard(contentPadding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = CronosCyan,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(11.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Extract into",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = check.output,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    GlassChip(
                        text = "Change",
                        onClick = onChooseFolder,
                    )
                }
            }
        }

        item {
            SectionLabel(
                text = "Space plan",
                trailing = if (viewModel.isRefreshingPlan) "Rechecking..." else null,
            )
        }

        item {
            SpacePlanCard(check = check, refreshing = viewModel.isRefreshingPlan)
        }

        if (check.inPlaceRefusal != null && !options.keepArchive) {
            item {
                WarningCard(
                    title = "This archive cannot shrink in place",
                    body = check.inPlaceRefusal ?: "",
                    color = Warning,
                )
            }
        }

        item {
            SectionLabel(text = "Options")
        }

        item {
            GlassCard(contentPadding = PaddingValues(18.dp)) {
                OptionSwitch(
                    title = "Keep the archive",
                    body = if (options.keepArchive) {
                        "The archive stays exactly as it is. Free space must hold every " +
                            "extracted file, like a standard extractor."
                    } else {
                        "The archive is cut away as files land. This is the in-place mode " +
                            "that needs the least free space."
                    },
                    checked = options.keepArchive,
                    enabled = !check.resuming && !check.orphan,
                    onCheckedChange = {
                        viewModel.updateOptions(options.copy(keepArchive = it))
                    },
                )
                if (check.resuming) {
                    Text(
                        text = "This setting is fixed by the extraction already in progress.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.7f),
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keep free space",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Text(
                            text = "A safety margin the engine never uses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 5, 10).forEach { gigabytes ->
                        GlassChip(
                            text = "${gigabytes} GB",
                            selected = options.keepFree == gigabytes * 1024L * 1024L * 1024L,
                            onClick = {
                                viewModel.updateOptions(
                                    options.copy(
                                        keepFree = gigabytes * 1024L * 1024L * 1024L,
                                    ),
                                )
                            },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                OptionSwitch(
                    title = "Verify before extracting",
                    body = "Read the whole archive and check every checksum before anything is deleted.",
                    checked = options.verifyFirst,
                    enabled = !check.resuming && !check.orphan,
                    onCheckedChange = {
                        viewModel.updateOptions(options.copy(verifyFirst = it))
                    },
                )
            }
        }

        if (check.existingCount > 0) {
            item {
                GlassCard(glow = Warning, contentPadding = PaddingValues(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            text = "${check.existingCount} existing " +
                                if (check.existingCount == 1) "file" else "files",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = check.existingFiles.take(3).joinToString("\n") { "· $it" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassChip(
                            text = "Replace them",
                            selected = options.replaceMode == "replace",
                            onClick = {
                                viewModel.updateOptions(options.copy(replaceMode = "replace"))
                            },
                        )
                        GlassChip(
                            text = "Keep existing",
                            selected = options.replaceMode == "skip",
                            onClick = {
                                viewModel.updateOptions(options.copy(replaceMode = "skip"))
                            },
                        )
                    }
                    if (options.replaceMode == "none") {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Choose what happens before extracting. Nothing is changed yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Warning,
                        )
                    }
                }
            }
        }

        item {
            if (check.orphan) {
                InfoNote(
                    icon = Icons.Rounded.CheckCircle,
                    color = Success,
                    text = "The extraction already finished. Finish the cleanup to remove the " +
                        "journal and the replaced-files folder.",
                )
            } else {
                val blocked = when {
                    !check.plan.fits -> "Not enough free space. Nothing will be changed."
                    check.inPlaceRefusal != null && !options.keepArchive ->
                        "Enable \"Keep the archive\" to extract this archive."
                    check.existingCount > 0 && options.replaceMode == "none" ->
                        "Choose what happens to the existing files."
                    else -> null
                }
                PrimaryButton(
                    text = when {
                        check.orphan -> "Finish cleanup"
                        check.resuming -> "Resume extraction"
                        options.keepArchive -> "Extract and keep archive"
                        else -> "Extract in place"
                    },
                    icon = if (check.orphan) Icons.Rounded.CheckCircle else Icons.Rounded.Shield,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = blocked == null,
                    onClick = { viewModel.startExtraction() },
                )
                if (blocked != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = blocked,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (!check.plan.fits) Danger else Warning,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "The plan is checked again by the engine immediately before it " +
                        "starts. If it no longer fits, it stops without changing anything.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.7f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = "View contents",
                    icon = Icons.Rounded.List,
                    modifier = Modifier.weight(1f),
                    enabled = !check.orphan,
                    onClick = onViewContents,
                )
                if (check.resuming) {
                    SecondaryButton(
                        text = "Restore",
                        icon = Icons.Rounded.Restore,
                        modifier = Modifier.weight(1f),
                        onClick = { confirmRestore = true },
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
                    text = "The parts not extracted yet become a standard archive again. Files " +
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
private fun SpacePlanCard(check: CheckData, refreshing: Boolean) {
    val plan = check.plan
    GlassCard(glow = if (plan.fits) CronosBlue else Danger) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Will it fit?",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            if (refreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = CronosCyan,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(8.dp))
            }
            StatusPill(
                text = if (plan.fits) "Fits" else "Not enough space",
                color = if (plan.fits) Success else Danger,
            )
        }
        Spacer(Modifier.height(14.dp))
        PlanBar(
            free = plan.free,
            needed = plan.needed,
            keepFree = plan.keepFree,
            fits = plan.fits,
        )
        Spacer(Modifier.height(14.dp))
        SpaceRow("Free now", formatBytes(plan.free))
        SpaceRow("Required at peak", formatBytes(plan.required))
        SpaceRow("Kept free", formatBytes(plan.keepFree))
        if (plan.fits) {
            SpaceRow("Free when finished", formatBytes(plan.freeAfter), color = Success)
        } else {
            SpaceRow(
                "Still needed",
                formatBytes((plan.required - plan.free).coerceAtLeast(0L)),
                color = Danger,
            )
        }
        if (plan.archiveKeptWhole) {
            Spacer(Modifier.height(10.dp))
            InfoNote(
                icon = Icons.Rounded.Info,
                color = Warning,
                text = "This is a compressed tar stream and the storage cannot free its middle. " +
                    "The whole archive stays until the end, so standard extraction space is needed.",
            )
        } else if (!plan.reclaims) {
            Spacer(Modifier.height(10.dp))
            InfoNote(
                icon = Icons.Rounded.Info,
                color = Warning,
                text = "The destination is on another volume, so freeing the archive does not " +
                    "make room for the output. Standard extraction space is needed.",
            )
        } else if (plan.solidBlockBound) {
            Spacer(Modifier.height(10.dp))
            InfoNote(
                icon = Icons.Rounded.Info,
                color = Warning,
                text = "A solid block is larger than the free space. Space is freed only once " +
                    "the whole block is decoded.",
            )
        }
    }
}

@Composable
private fun SpaceRow(label: String, value: String, color: Color = TextPrimary) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun OptionSwitch(
    title: String,
    body: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun InfoNote(
    icon: ImageVector,
    color: Color,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun WarningCard(title: String, body: String, color: Color) {
    GlassCard(glow = color, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(17.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}
