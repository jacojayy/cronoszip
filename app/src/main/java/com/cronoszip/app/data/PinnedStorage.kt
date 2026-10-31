package com.cronoszip.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/**
 * A pinned storage location: either a SAF tree (picked through the system folder picker) or a plain
 * root-backed path. Ported from BFE's `PinnedStorage` so "Add app storage" actually grants access
 * instead of asking the user to type a path that Android will not let the app read.
 */
data class PinnedLocation(
    val label: String,
    val kind: String,
    val treeUri: Uri?,
    val path: String?,
    val packageName: String?,
) {
    val isRoot: Boolean get() = kind == KIND_ROOT
    val key: String get() = if (isRoot) "root:$path" else treeUri.toString()

    companion object {
        const val KIND_SAF = "saf"
        const val KIND_ROOT = "root"
    }
}

object PinnedStorage {
    private const val PREFS = "cronoszip.pinned"
    private const val KEY = "pinnedLocations"

    fun list(context: Context): List<PinnedLocation> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val kind = o.optString("kind").ifBlank { PinnedLocation.KIND_SAF }
                val label = o.optString("label").ifBlank { "Storage" }
                val pkg = o.optString("pkg").takeIf { it.isNotBlank() }
                if (kind == PinnedLocation.KIND_ROOT) {
                    val path = o.optString("path").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    PinnedLocation(label, kind, null, path, pkg)
                } else {
                    val uri = o.optString("uri").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    PinnedLocation(label, kind, Uri.parse(uri), null, pkg)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Pin a SAF tree and keep the grant across reboots. Idempotent on the Uri. */
    fun add(context: Context, label: String, treeUri: Uri, packageName: String?) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }
        val item = PinnedLocation(label, PinnedLocation.KIND_SAF, treeUri, null, packageName)
        save(context, list(context).filterNot { it.key == item.key } + item)
    }

    /** Pin a root-backed path (no consent step). Idempotent on the path. */
    fun addRoot(context: Context, label: String, path: String, packageName: String?) {
        val item = PinnedLocation(label, PinnedLocation.KIND_ROOT, null, path, packageName)
        save(context, list(context).filterNot { it.key == item.key } + item)
    }

    /** Unpin [item]; releases the SAF grant when it is a SAF pin. */
    fun remove(context: Context, item: PinnedLocation) {
        save(context, list(context).filterNot { it.key == item.key })
        val uri = item.treeUri ?: return
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }
    }

    private fun save(context: Context, items: List<PinnedLocation>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject()
                    .put("label", it.label)
                    .put("kind", it.kind)
                    .put("uri", it.treeUri?.toString() ?: "")
                    .put("path", it.path ?: "")
                    .put("pkg", it.packageName ?: ""),
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, arr.toString())
            .apply()
    }

    /**
     * Maps a SAF tree Uri back to a normal filesystem path when the provider exposes one
     * (device storage, Downloads). Returns null for providers that can only be browsed through
     * the Documents API, which this file manager does not support yet.
     */
    fun resolveTreePath(treeUri: Uri): String? {
        val authority = treeUri.authority ?: return null
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return null
        return when (authority) {
            "com.android.externalstorage.documents" -> externalStoragePath(docId)
            "com.android.providers.downloads.documents" -> downloadsPath(docId)
            else -> null
        }
    }

    /** Resolves a document (not tree) Uri to a real path when the provider is device storage. */
    fun resolveDocumentPath(documentUri: Uri): String? {
        val authority = documentUri.authority ?: return null
        val docId = runCatching { DocumentsContract.getDocumentId(documentUri) }.getOrNull() ?: return null
        return when (authority) {
            "com.android.externalstorage.documents" -> externalStoragePath(docId)
            "com.android.providers.downloads.documents" -> downloadsPath(docId)
            else -> null
        }
    }

    private fun externalStoragePath(docId: String): String? {
        val parts = docId.split(':', limit = 2)
        val volume = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
        if (volume.equals("home", ignoreCase = true)) return null
        val rest = parts.getOrNull(1).orEmpty()
        val root = if (volume.equals("primary", ignoreCase = true)) {
            Environment.getExternalStorageDirectory()
        } else {
            File("/storage", volume)
        }
        val resolved = if (rest.isBlank()) root else File(root, rest)
        return resolved.absolutePath.takeIf { File(it).canRead() }
    }

    private fun downloadsPath(docId: String): String? {
        if (!docId.startsWith("raw:")) return null
        val path = docId.removePrefix("raw:")
        return path.takeIf { File(it).canRead() }
    }
}

/** An installed app that exposes SAF storage (a DocumentsProvider). */
data class DocProviderApp(
    val label: String,
    val packageName: String,
    val authority: String,
)

object DocumentsProviderApps {
    private const val ACTION_DOCUMENTS_PROVIDER = "android.content.action.DOCUMENTS_PROVIDER"

    fun list(context: Context): List<DocProviderApp> {
        val pm = context.packageManager
        val myPackage = context.packageName
        return try {
            @Suppress("DEPRECATION")
            pm.queryIntentContentProviders(Intent(ACTION_DOCUMENTS_PROVIDER), 0)
                .mapNotNull { info ->
                    val provider = info.providerInfo ?: return@mapNotNull null
                    if (provider.packageName == myPackage) return@mapNotNull null
                    val label = runCatching { provider.loadLabel(pm).toString() }.getOrNull()
                        ?: runCatching {
                            pm.getApplicationInfo(provider.packageName, 0).loadLabel(pm).toString()
                        }.getOrNull()
                        ?: provider.packageName
                    DocProviderApp(label, provider.packageName, provider.authority)
                }
                .distinctBy { it.packageName + "/" + it.authority }
                .sortedBy { it.label.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Seeds ACTION_OPEN_DOCUMENT_TREE at the provider's root so the user only confirms the folder. */
    fun initialTreeUri(context: Context, authority: String): Uri? = try {
        val rootsUri = DocumentsContract.buildRootsUri(authority)
        context.contentResolver.query(
            rootsUri,
            arrayOf(
                DocumentsContract.Root.COLUMN_ROOT_ID,
                DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val docIndex = cursor.getColumnIndex(DocumentsContract.Root.COLUMN_DOCUMENT_ID)
                val rootIndex = cursor.getColumnIndex(DocumentsContract.Root.COLUMN_ROOT_ID)
                val documentId = if (docIndex >= 0 && !cursor.isNull(docIndex)) cursor.getString(docIndex) else null
                val rootId = if (rootIndex >= 0) cursor.getString(rootIndex) else null
                (documentId ?: rootId)?.let { DocumentsContract.buildTreeDocumentUri(authority, it) }
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}
