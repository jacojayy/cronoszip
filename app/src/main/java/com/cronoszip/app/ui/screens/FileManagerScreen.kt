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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cronoszip.app.data.BrowserMode
import com.cronoszip.app.data.FileItem
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
fun FileManagerScreen(
    viewModel: CronosViewModel,
    onBack: () -> Unit,
    onOpenArchive: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasStorageAccess(context)) }
    val browser by viewModel.browser.collectAsState()
    val roots = remember(context) { managerRoots(context) }
    var current by remember { mutableStateOf(roots.firstOrNull()?.second ?: "") }
    var items by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val legacyPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        granted = hasStorageAccess(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val now = hasStorageAccess(context)
                granted = now
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun load(path: String) {
        if (path.isBlank()) return
        current = path
        loading = true
        error = null
        val dir = File(path)
        val children = try { dir.listFiles() } catch (e: SecurityException) { null }
        if (children == null) {
            loading = false
            error = "Cannot read this folder."
            items = emptyList()
            return
        }
        items = children
            .filterNot { it.name.startsWith(".") }
            .sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() }))
            .map {
                FileItem(
                    name = it.name,
                    path = it.absolutePath,
                    directory = it.isDirectory,
                    size = if (it.isDirectory) 0L else it.length(),
                    modified = it.lastModified(),
                    archive = it.isFile && isArchive(it.name),
                )
            }
        loading = false
    }

    LaunchedEffect(granted, roots) {
        if (granted && current.isNotBlank()) {
            load(current)
        }
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        ScreenHeader(title = "File manager", subtitle = "App storage · External storage", onBack = onBack)

        if (!granted) {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                GlassCard(glow = CronosCyan) {
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(CronosCyan.copy(alpha = 0.14f)).border(1.dp, CronosCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = CronosCyan, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Storage access is needed", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("CronosZip needs access to read files and open archives across storage.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(text = "Allow access", icon = Icons.Rounded.Storage, modifier = Modifier.fillMaxWidth(), onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) requestAllFilesAccess(context)
                        else {
                            val perms = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE) else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                            legacyPermission.launch(perms)
                        }
                    })
                }
            }
            return@Column
        }

        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(roots) { root ->
                GlassChip(text = root.first, selected = current == root.second, onClick = { load(root.second) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = CronosCyan, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = current, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CronosCyan) }
                error != null -> Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) { GlassCard(glow = Warning) { Text("Cannot read folder", style = MaterialTheme.typography.titleMedium, color = TextPrimary); Spacer(Modifier.height(6.dp)); Text(error ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary); Spacer(Modifier.height(12.dp)); SecondaryButton("Try again", modifier = Modifier.fillMaxWidth(), onClick = { load(current) }) } }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    val parent = File(current).parent
                    if (parent != null && parent != current) {
                        item { Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { load(parent) }.background(GlassWhite.copy(alpha = 0.035f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Upload, contentDescription = null, tint = TextSecondary); Spacer(Modifier.width(10.dp)); Text("Up one folder", color = TextSecondary) } }
                    }
                    items(items) { item ->
                        GlassCard(modifier = Modifier.fillMaxWidth().clickable {
                            if (item.directory) load(item.path)
                            else if (isArchive(item.name)) onOpenArchive(item.path)
                        }, contentPadding = PaddingValues(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(if (item.directory) Brush.linearGradient(listOf(CronosCyan.copy(alpha = 0.24f), CronosBlue.copy(alpha = 0.08f))) else Brush.linearGradient(listOf(CronosBlue.copy(alpha = 0.3f), CronosCyan.copy(alpha = 0.1f)))), border = BorderStroke(1.dp, if (item.directory) CronosCyan.copy(alpha = 0.25f) else CronosBlue.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Icon(if (item.directory) Icons.Rounded.Folder else if (isArchive(item.name)) Icons.Rounded.Archive else Icons.Rounded.Description, contentDescription = null, tint = if (item.directory) CronosCyan else Color(0xFF9CC7FF))
                                }
                                Spacer(Modifier.width(13.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(if (item.directory) "Folder" else "${formatBytes(item.size)} · ${formatTime(item.modified)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                if (item.directory || isArchive(item.name)) Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary.copy(alpha = 0.55f))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun managerRoots(context: android.content.Context): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val external = android.os.Environment.getExternalStorageDirectory()
    external?.let { list.add("Internal" to it.absolutePath) }
    val download = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
    if (download != null && download.absolutePath != external?.absolutePath) list.add("Downloads" to download.absolutePath)
    val documents = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
    if (documents != null) list.add("Documents" to documents.absolutePath)
    val movies = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES)
    if (movies != null) list.add("Movies" to movies.absolutePath)
    val music = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MUSIC)
    if (music != null) list.add("Music" to music.absolutePath)
    val pictures = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
    if (pictures != null) list.add("Pictures" to pictures.absolutePath)
    context.getExternalFilesDir(null)?.let { list.add("App storage (external)" to it.absolutePath) }
    context.filesDir?.let { list.add("App storage (internal)" to it.absolutePath) }
    context.cacheDir?.let { list.add("App cache" to it.absolutePath) }
    return list.distinctBy { it.second }
}

import androidx.compose.foundation.BorderStroke
