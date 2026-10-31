package com.cronoszip.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cronoszip.app.ui.components.GlassCard
import com.cronoszip.app.ui.components.PrimaryButton
import com.cronoszip.app.ui.components.ScreenHeader
import com.cronoszip.app.ui.components.SecondaryButton
import com.cronoszip.app.ui.theme.CronosBlue
import com.cronoszip.app.ui.theme.CronosCyan
import com.cronoszip.app.ui.theme.GlassWhite
import com.cronoszip.app.ui.theme.Success
import com.cronoszip.app.ui.theme.TextPrimary
import com.cronoszip.app.ui.theme.TextSecondary
import com.cronoszip.app.ui.theme.Warning
import com.cronoszip.app.util.formatBytes
import com.cronoszip.app.viewmodel.CronosViewModel

@Composable
fun UpdaterScreen(
    viewModel: CronosViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val update by viewModel.update.collectAsState()

    LaunchedEffect(Unit) {
        if (update.update == null && !update.checking) {
            viewModel.checkForUpdates()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ScreenHeader(title = "Updater", subtitle = "In-place updates with verified SHA-256", onBack = onBack) }

        item {
            GlassCard(glow = CronosCyan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(CronosBlue.copy(alpha = 0.34f), CronosCyan.copy(alpha = 0.12f)),
                                ),
                            )
                            .border(1.dp, CronosBlue.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.SystemUpdate, contentDescription = null, tint = CronosCyan, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CronosZip", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Current: v${viewModel.appVersion} (${viewModel.appVersionCode})", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    if (update.checking) CircularProgressIndicator(color = CronosCyan, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(text = if (update.checking) "Checking..." else "Check latest", modifier = Modifier.weight(1f), onClick = { viewModel.checkForUpdates() }, enabled = !update.checking && !update.downloading)
                    PrimaryButton(text = "Open GitHub releases", icon = Icons.Rounded.Download, modifier = Modifier.weight(1f), onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jacojayy/cronoszip/releases"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    })
                }
            }
        }

        update.update?.let { info ->
            item {
                GlassCard(glow = CronosBlue) {
                    Text("Latest release", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(info.tagName.ifBlank { info.versionName }, style = MaterialTheme.typography.bodyMedium, color = CronosCyan)
                    if (info.publishedAt.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(info.publishedAt, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = if (info.sha256.isNotBlank()) Success else Warning, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (info.sha256.isNotBlank()) "SHA-256 present for verification" else "No .sha256 asset found", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    if (info.apkName.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Asset: ${info.apkName} (${formatBytes(info.apkSize)})", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    if (info.sha256.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("SHA-256: ${info.sha256.take(16)}...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Spacer(Modifier.height(16.dp))
                    if (update.downloading) {
                        LinearProgressIndicator(progress = { if (update.total <= 0) 0.4f else (update.progress.toFloat() / update.total.toFloat()).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = CronosCyan, trackColor = GlassWhite.copy(alpha = 0.08f))
                        Spacer(Modifier.height(6.dp))
                        Text("${formatBytes(update.progress)} / ${formatBytes(update.total)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    } else if (update.installable) {
                        PrimaryButton(text = "Install update (in place)", icon = Icons.Rounded.SystemUpdate, modifier = Modifier.fillMaxWidth(), onClick = {
                            val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                                context.packageManager.canRequestPackageInstalls()
                            if (!allowed) {
                                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } else {
                                viewModel.installUpdate()
                            }
                        })
                        Spacer(Modifier.height(10.dp))
                        Text("Download verified with the .sha256 from the latest release.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    } else {
                        PrimaryButton(text = "Download & verify SHA-256", icon = Icons.Rounded.Download, modifier = Modifier.fillMaxWidth(), onClick = { viewModel.downloadUpdate() }, enabled = info.apkUrl.isNotBlank())
                    }
                }
            }
        }

        item {
            GlassCard(contentPadding = PaddingValues(16.dp)) {
                Text("How it works", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text("The updater fetches the latest GitHub release, downloads the .sha256 asset, downloads the APK, verifies its SHA-256, and launches the installer with FileProvider. Because releases are signed with the same keystore, Android can update without uninstalling.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                Text("Allow \"Install unknown apps\" for CronosZip if prompted on Android 8+.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        if (update.message.isNotBlank() && update.error.isNullOrBlank() && !update.downloading && !update.checking) {
            item {
                GlassCard(glow = CronosCyan, contentPadding = PaddingValues(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, tint = CronosCyan)
                        Spacer(Modifier.width(10.dp))
                        Text(update.message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        }

        update.error?.let { err ->
            item {
                GlassCard(glow = Warning, contentPadding = PaddingValues(14.dp)) {
                    Text("Update error", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(err, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}
