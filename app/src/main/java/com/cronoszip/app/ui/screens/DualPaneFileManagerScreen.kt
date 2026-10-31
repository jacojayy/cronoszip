package com.cronoszip.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cronoszip.app.data.FileClipboard
import com.cronoszip.app.data.FileItem
import com.cronoszip.app.data.FileOperations
import com.cronoszip.app.ui.components.GlassCard
import com.cronoszip.app.ui.components.GlassChip
import com.cronoszip.app.ui.components.PrimaryButton
import com.cronoszip.app.ui.components.ScreenHeader
import com.cronoszip.app.ui.components.SecondaryButton
import com.cronoszip.app.ui.theme.CronosBlue
import com.cronoszip.app.ui.theme.CronosCyan
import com.cronoszip.app.ui.theme.GlassWhite
import com.cronoszip.app.ui.theme.TextPrimary
import com.cronoszip.app.ui.theme.TextSecondary
import com.cronoszip.app.ui.theme.Warning
import com.cronoszip.app.util.formatBytes
import com.cronoszip.app.util.formatTime
import com.cronoszip.app.util.hasStorageAccess
import com.cronoszip.app.util.isArchive
import com.cronoszip.app.util.requestAllFilesAccess
import com.cronoszip.app.viewmodel.CronosViewModel
import java.io.File

@Composable
fun DualPaneFileManagerScreen(
    viewModel: CronosViewModel,
    onBack: () -> Unit,
    onOpenArchive: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasStorageAccess(context)) }
    val roots = remember(context) { managerRoots(context) }
    var refreshToken by remember { mutableStateOf(0) }

    val legacyPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        granted = hasStorageAccess(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = hasStorageAccess(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        ScreenHeader(title = "Dual panel side view", subtitle = "Independent panes · App storage + external", onBack = onBack)
        if (!granted) {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                GlassCard(glow = CronosCyan) {
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(CronosCyan.copy(alpha = 0.14f)).border(1.dp, CronosCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = CronosCyan, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Storage access needed", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("Grant all files access to browse shared storage in both panes.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(text = "Allow access", icon = Icons.Rounded.Storage, modifier = Modifier.fillMaxWidth(), onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) requestAllFilesAccess(context)
                        else legacyPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    })
                }
            }
            return@Column
        }

        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pane(modifier = Modifier.weight(1f), roots = roots, start = roots.firstOrNull()?.second.orEmpty(), refreshToken = refreshToken, onChanged = { refreshToken++ }, onOpenArchive = onOpenArchive)
            Pane(modifier = Modifier.weight(1f), roots = roots, start = roots.getOrNull(1)?.second ?: roots.firstOrNull()?.second.orEmpty(), refreshToken = refreshToken, onChanged = { refreshToken++ }, onOpenArchive = onOpenArchive)
        }
    }
}

@Composable
private fun Pane(
    modifier: Modifier = Modifier,
    roots: List<Pair<String, String>>,
    start: String,
    refreshToken: Int,
    onChanged: () -> Unit,
    onOpenArchive: (String) -> Unit,
) {
    var current by remember(start) { mutableStateOf(start) }
    var entries by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var action by remember { mutableStateOf<FileItem?>(null) }

    fun load(path: String) {
        if (path.isBlank()) return
        current = path
        loading = true
        error = null
        val dir = File(path)
        val children = try { dir.listFiles() } catch (e: SecurityException) { null }
        if (children == null) {
            loading = false
            error = "Cannot read"
            entries = emptyList()
            return
        }
        entries = children.filterNot { it.name.startsWith(".") }.sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() })).map {
            FileItem(name = it.name, path = it.absolutePath, directory = it.isDirectory, size = if (it.isDirectory) 0L else it.length(), modified = it.lastModified(), archive = it.isFile && isArchive(it.name))
        }
        loading = false
    }

    LaunchedEffect(current, refreshToken) { if (current.isNotBlank()) load(current) }

    GlassCard(modifier = modifier.fillMaxHeight(), contentPadding = PaddingValues(10.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 6.dp)) {
                items(roots) { root -> GlassChip(text = root.first.take(9), selected = current == root.second, onClick = { load(root.second) }) }
                item {
                    GlassChip(
                        text = if (FileClipboard.cut) "Move here" else "Paste",
                        selected = FileClipboard.path != null,
                        onClick = if (FileClipboard.path != null) {
                            {
                                FileOperations.paste(File(current))
                                    .onSuccess { onChanged() }
                                    .onFailure { error = it.message }
                                load(current)
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = CronosCyan, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = current, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.weight(1f)) {
                when {
                    loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CronosCyan, strokeWidth = 2.dp) }
                    error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error ?: "", style = MaterialTheme.typography.bodySmall, color = Warning) }
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val parent = File(current).parent
                        if (parent != null && parent != current) {
                            item { Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { load(parent) }.background(GlassWhite.copy(alpha = 0.03f)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Upload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Up", color = TextSecondary, style = MaterialTheme.typography.bodySmall) } }
                        }
                        items(entries) { item ->
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                                if (item.directory) load(item.path)
                                else if (isArchive(item.name)) onOpenArchive(item.path)
                            }.background(GlassWhite.copy(alpha = 0.035f)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(if (item.directory) Brush.linearGradient(listOf(CronosCyan.copy(alpha = 0.22f), CronosBlue.copy(alpha = 0.08f))) else Brush.linearGradient(listOf(CronosBlue.copy(alpha = 0.26f), CronosCyan.copy(alpha = 0.1f)))), contentAlignment = Alignment.Center) {
                                    Icon(if (item.directory) Icons.Rounded.Folder else if (isArchive(item.name)) Icons.Rounded.Archive else Icons.Rounded.Description, contentDescription = null, tint = if (item.directory) CronosCyan else Color(0xFF9CC7FF), modifier = Modifier.size(17.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(if (item.directory) "Folder" else formatBytes(item.size), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                if (item.directory || isArchive(item.name)) Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                IconButton(onClick = { action = item }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "File actions", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    action?.let { item ->
        Dialog(onDismissRequest = { action = null }) {
            GlassCard(glow = CronosCyan) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(12.dp))
                SecondaryButton(
                    text = "Copy",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        FileClipboard.path = item.path
                        FileClipboard.cut = false
                        action = null
                    },
                )
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    text = "Cut / move",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        FileClipboard.path = item.path
                        FileClipboard.cut = true
                        action = null
                    },
                )
            }
        }
    }
}
