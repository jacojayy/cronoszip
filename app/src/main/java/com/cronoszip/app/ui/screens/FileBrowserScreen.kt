package com.cronoszip.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.cronoszip.app.util.requestAllFilesAccess
import com.cronoszip.app.viewmodel.CronosViewModel

@Composable
fun FileBrowserScreen(
    viewModel: CronosViewModel,
    mode: BrowserMode,
    onBack: () -> Unit,
    onFolderChosen: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasStorageAccess(context)) }
    val browser by viewModel.browser.collectAsState()

    val legacyPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        granted = hasStorageAccess(context)
        viewModel.refreshBrowserAfterPermission()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val now = hasStorageAccess(context)
                granted = now
                if (now) viewModel.refreshBrowserAfterPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(mode) {
        viewModel.enterBrowser(mode)
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        ScreenHeader(
            title = if (mode == BrowserMode.Folder) "Choose destination" else "Choose archive",
            subtitle = when (mode) {
                BrowserMode.Folder -> "Files are extracted into this folder"
                BrowserMode.Archive -> "Any part of a split archive works"
            },
            onBack = onBack,
        )

        if (!granted) {
            Box(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                PermissionCard(
                    onAllow = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            requestAllFilesAccess(context)
                        } else {
                            val permissions = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                )
                            } else {
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            legacyPermission.launch(permissions)
                        }
                    },
                )
            }
            return@Column
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CronosBlue.copy(alpha = 0.16f))
                        .border(1.dp, CronosBlue.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null,
                        tint = CronosCyan,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = browser.current.ifBlank { "Storage" },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(browser.roots, key = { it.path }) { root ->
                    GlassChip(
                        text = root.label,
                        selected = browser.current == root.path,
                        onClick = { viewModel.navigateBrowser(root.path) },
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    browser.loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = CronosCyan,
                                strokeWidth = 3.dp,
                                trackColor = Color.White.copy(alpha = 0.08f),
                            )
                        }
                    }
                    browser.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            GlassCard(glow = Warning) {
                                Text(
                                    text = "Cannot read this folder",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = browser.error ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                                Spacer(Modifier.height(14.dp))
                                SecondaryButton(
                                    text = "Try again",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { viewModel.navigateBrowser(browser.current) },
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 2.dp,
                                bottom = 20.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(9.dp),
                        ) {
                            val parent = browser.parent
                            if (parent != null) {
                                item(key = "up") {
                                    UpRow(onClick = { viewModel.navigateBrowser(parent) })
                                }
                            }
                            items(browser.items, key = { it.path }) { item ->
                                FileRow(
                                    item = item,
                                    onClick = {
                                        if (item.directory) {
                                            viewModel.navigateBrowser(item.path)
                                        } else if (item.archive) {
                                            viewModel.checkArchive(item.path)
                                        }
                                    },
                                )
                            }
                            if (browser.items.isEmpty() && parent == null) {
                                item(key = "empty") {
                                    EmptyFolder()
                                }
                            }
                            if (browser.items.isEmpty() && parent != null) {
                                item(key = "empty-nested") {
                                    EmptyFolder()
                                }
                            }
                            item(key = "footer") {
                                Text(
                                    text = if (mode == BrowserMode.Archive) {
                                        "Showing folders and archives: ZIP, 7Z, TAR, TAR.GZ, " +
                                            "TGZ, RAR, split .001+ sets and self-extracting ZIPs."
                                    } else {
                                        "The archive shrinks in place; its output can live " +
                                            "anywhere on the same volume."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary.copy(alpha = 0.65f),
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (mode == BrowserMode.Folder) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    PrimaryButton(
                        text = "Use this folder",
                        icon = Icons.Rounded.Upload,
                        enabled = browser.current.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onFolderChosen(browser.current) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(onAllow: () -> Unit) {
    GlassCard(glow = CronosCyan) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CronosCyan.copy(alpha = 0.14f))
                .border(1.dp, CronosCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = CronosCyan,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Storage access is needed",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "CronosZip changes the archive file itself, so it needs access to the folder " +
                "the archive lives in. Android calls this all-files access. Nothing is read or " +
                "changed until you pick an archive and confirm its plan.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text = "Allow access",
            icon = Icons.Rounded.Storage,
            modifier = Modifier.fillMaxWidth(),
            onClick = onAllow,
        )
    }
}

@Composable
private fun UpRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite.copy(alpha = 0.035f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Upload,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Up one folder",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

@Composable
private fun FileRow(item: FileItem, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        contentPadding = PaddingValues(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (item.directory) {
                            Brush.linearGradient(
                                listOf(CronosCyan.copy(alpha = 0.24f), CronosBlue.copy(alpha = 0.08f)),
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(CronosBlue.copy(alpha = 0.3f), CronosCyan.copy(alpha = 0.1f)),
                            )
                        },
                    )
                    .border(
                        1.dp,
                        if (item.directory) {
                            CronosCyan.copy(alpha = 0.25f)
                        } else {
                            CronosBlue.copy(alpha = 0.3f)
                        },
                        RoundedCornerShape(13.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (item.directory) {
                        Icons.Rounded.Folder
                    } else {
                        Icons.Rounded.Archive
                    },
                    contentDescription = null,
                    tint = if (item.directory) CronosCyan else Color(0xFF9CC7FF),
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (item.directory) {
                        "Folder"
                    } else {
                        listOf(
                            formatBytes(item.size),
                            formatTime(item.modified).takeIf { it.isNotBlank() },
                        ).filterNotNull().joinToString(" · ")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun EmptyFolder() {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "No archives or folders here.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}
