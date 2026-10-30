package com.cronoszip.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiEnvelope(
    val ok: Boolean,
    val code: String = "OK",
    val message: String = "",
    val data: JsonElement? = null,
)

class EngineException(
    val code: String,
    override val message: String,
    val data: JsonElement? = null,
) : Exception(message)

@Serializable
data class CheckConfig(
    val archive: String,
    val output: String? = null,
    val keepArchive: Boolean = false,
    val keepFree: Long = DEFAULT_KEEP_FREE,
    val password: String? = null,
)

@Serializable
data class ExtractConfig(
    val archive: String,
    val output: String? = null,
    val keepArchive: Boolean = false,
    val replace: String = "none",
    val verifyFirst: Boolean = false,
    val keepFree: Long = DEFAULT_KEEP_FREE,
    val password: String? = null,
)

@Serializable
data class ArchiveConfig(
    val archive: String,
    val password: String? = null,
)

@Serializable
data class RestoreConfig(
    val archive: String,
    val output: String? = null,
)

@Serializable
data class PlanData(
    val free: Long,
    val needed: Long,
    val keepFree: Long,
    val fits: Boolean,
    val freeAfter: Long,
    val keepArchive: Boolean,
    val normalNeeds: Long,
    val inPlaceNeeds: Long,
    val either: Boolean,
    val reclaims: Boolean,
    val solidBlockBound: Boolean,
    val archiveKeptWhole: Boolean,
) {
    val required: Long get() = needed + keepFree
}

@Serializable
data class CheckData(
    val archive: String,
    val displayName: String,
    val format: String,
    val archiveSize: Long,
    val entryCount: Int,
    val uncompressedSize: Long,
    val totalSize: Long,
    val committedLen: Long = 0,
    val encrypted: Boolean,
    val resuming: Boolean,
    val resumeKeepsArchive: Boolean = false,
    val orphan: Boolean = false,
    val output: String,
    val existingFiles: List<String> = emptyList(),
    val existingCount: Int = 0,
    val singleRoot: Boolean = false,
    val inPlaceRefusal: String? = null,
    val plan: PlanData,
)

@Serializable
data class ExtractSummary(
    val paused: Boolean = false,
    val kept: Boolean = false,
    val itemCount: Int = 0,
    val totalSize: Long = 0,
    val output: String = "",
    val archive: String = "",
    val orphanCleaned: Boolean = false,
)

@Serializable
data class TestSummary(
    val entries: Int,
    val bytes: Long,
    val archive: String,
)

@Serializable
data class EntryData(
    val path: String,
    val size: Long,
    val kind: String,
    val target: String? = null,
    val mode: Long? = null,
    val mtime: Long? = null,
)

@Serializable
data class ListData(
    val archive: String,
    val displayName: String,
    val format: String,
    val archiveSize: Long,
    val totalSize: Long,
    val entryCount: Int,
    val encrypted: Boolean,
    val resuming: Boolean,
    val entries: List<EntryData>,
    val truncated: Boolean,
)

@Serializable
data class RestoreResult(
    val archive: String,
    val format: String,
    val entries: Int,
    val output: String,
    val kept: Boolean,
    val setAside: Int,
)

@Serializable
data class NativeEvent(
    val type: String,
    val phase: String? = null,
    val message: String? = null,
    val done: Long? = null,
    val total: Long? = null,
    val committed: Long? = null,
    val archiveSize: Long? = null,
    val current: String? = null,
)

@Serializable
data class HistoryEntry(
    val path: String,
    val name: String,
    val format: String = "",
    val timestamp: Long,
    val status: String,
)

sealed interface UiEffect {
    data class OpenSetup(val check: CheckData) : UiEffect
    data class NoSpace(val check: CheckData) : UiEffect
    data class PasswordNeeded(val wrong: Boolean) : UiEffect
    data class Message(val text: String) : UiEffect
    data object ExtractionStarted : UiEffect
    data class RestoreCompleted(val message: String) : UiEffect
}

data class CheckAttempt(
    val check: CheckData? = null,
    val passwordNeeded: Boolean = false,
    val wrongPassword: Boolean = false,
    val error: String? = null,
)

enum class BrowserMode { Archive, Folder }

data class StorageRoot(
    val label: String,
    val path: String,
)

data class FileItem(
    val name: String,
    val path: String,
    val directory: Boolean,
    val size: Long,
    val modified: Long,
    val archive: Boolean,
)

data class BrowserState(
    val mode: BrowserMode = BrowserMode.Archive,
    val current: String = "",
    val roots: List<StorageRoot> = emptyList(),
    val items: List<FileItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
) {
    val parent: String? get() = current.takeIf { it.isNotEmpty() }?.let { path ->
        val parent = java.io.File(path).parent
        if (parent != null && parent != path) parent else null
    }
}

data class SetupOptions(
    val keepArchive: Boolean = false,
    val replaceMode: String = "none",
    val verifyFirst: Boolean = false,
    val keepFree: Long = DEFAULT_KEEP_FREE,
)

data class ExtractionState(
    val running: Boolean = false,
    val phase: String = "idle",
    val message: String = "",
    val done: Long = 0,
    val total: Long = 0,
    val committed: Long = 0,
    val archiveSize: Long = 0,
    val currentFile: String? = null,
    val speed: Double = 0.0,
    val startedAt: Long = 0,
    val cancelling: Boolean = false,
    val paused: Boolean = false,
    val success: Boolean = false,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val summary: ExtractSummary? = null,
    val logs: List<String> = emptyList(),
    val output: String = "",
    val keepArchive: Boolean = false,
) {
    val fraction: Float
        get() = if (total <= 0) 0f else (done.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}

data class TestState(
    val running: Boolean = false,
    val done: Long = 0,
    val total: Long = 0,
    val message: String? = null,
    val passed: Boolean? = null,
    val error: String? = null,
)

data class DetailsState(
    val loading: Boolean = false,
    val list: ListData? = null,
    val error: String? = null,
)

data class PasswordRequest(
    val wrong: Boolean,
)

const val DEFAULT_KEEP_FREE = 2L * 1024L * 1024L * 1024L
