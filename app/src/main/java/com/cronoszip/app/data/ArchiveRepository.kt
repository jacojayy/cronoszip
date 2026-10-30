package com.cronoszip.app.data

import com.cronoszip.app.engine.NativeBridge
import com.cronoszip.app.engine.NativeEventCallback
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class ArchiveRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun check(config: CheckConfig): CheckData =
        decode(native { NativeBridge.nativeCheck(json.encodeToString(config)) })

    fun list(config: ArchiveConfig): ListData =
        decode(native { NativeBridge.nativeList(json.encodeToString(config)) })

    fun test(config: ArchiveConfig, onEvent: (NativeEvent) -> Unit): TestSummary =
        decode(
            native {
                NativeBridge.nativeTest(
                    json.encodeToString(config),
                    NativeEventCallback { raw -> emit(raw, onEvent) },
                )
            },
        )

    fun extract(config: ExtractConfig, onEvent: (NativeEvent) -> Unit): ExtractSummary =
        decode(
            native {
                NativeBridge.nativeExtract(
                    json.encodeToString(config),
                    NativeEventCallback { raw -> emit(raw, onEvent) },
                )
            },
        )

    fun restore(config: RestoreConfig): RestoreResult =
        decode(native { NativeBridge.nativeRestore(json.encodeToString(config)) })

    fun cancel() {
        if (NativeBridge.loaded) NativeBridge.nativeCancel()
    }

    fun isEngineAvailable(): Boolean = NativeBridge.loaded

    private inline fun native(block: () -> String): String {
        if (!NativeBridge.loaded) {
            throw EngineException(
                "NATIVE_MISSING",
                "The extraction engine is missing from this build. Rebuild the app with " +
                    "rust/build-android.sh so libcronoszip_jni.so is packaged.",
            )
        }
        return block()
    }

    private fun emit(raw: String, onEvent: (NativeEvent) -> Unit) {
        val event = runCatching { json.decodeFromString<NativeEvent>(raw) }.getOrNull() ?: return
        onEvent(event)
    }

    private inline fun <reified T> decode(raw: String): T {
        val envelope = try {
            json.decodeFromString<ApiEnvelope>(raw)
        } catch (error: Exception) {
            throw EngineException("BAD_RESPONSE", "The engine returned an unreadable reply.", null)
        }
        if (!envelope.ok) {
            throw EngineException(envelope.code, envelope.message, envelope.data)
        }
        val data = envelope.data
            ?: throw EngineException("BAD_RESPONSE", "The engine returned no result.", null)
        return try {
            json.decodeFromJsonElement<T>(data)
        } catch (error: Exception) {
            throw EngineException("BAD_RESPONSE", "The engine result could not be read.", data)
        }
    }
}
