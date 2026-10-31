package com.cronoszip.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.io.FileNotFoundException

object FileClipboard {
    var path by mutableStateOf<String?>(null)
    var cut by mutableStateOf(false)

    fun clear() {
        path = null
        cut = false
    }
}

object FileOperations {
    fun createFolder(parent: File, name: String): Result<File> = runCatching {
        problemWith(name)?.let { throw IllegalArgumentException(it) }
        val target = File(parent, name)
        if (target.exists()) throw IllegalStateException("A file with that name already exists.")
        if (!target.mkdirs()) throw IllegalStateException("The folder could not be created.")
        target
    }

    fun rename(file: File, name: String): Result<File> = runCatching {
        problemWith(name)?.let { throw IllegalArgumentException(it) }
        val target = File(file.parentFile, name)
        if (target.exists()) throw IllegalStateException("A file with that name already exists.")
        if (!file.renameTo(target)) throw IllegalStateException("The item could not be renamed.")
        target
    }

    fun delete(file: File): Result<Unit> = runCatching {
        if (!file.deleteRecursively()) throw IllegalStateException("The item could not be deleted.")
    }

    fun copy(src: File, destDir: File): Result<File> = runCatching {
        if (isInside(src, destDir)) throw IllegalArgumentException("A folder cannot be copied into itself.")
        val target = uniqueTarget(destDir, src.name)
        copyTo(src, target)
        target
    }

    fun move(src: File, destDir: File): Result<File> = runCatching {
        if (isInside(src, destDir)) throw IllegalArgumentException("A folder cannot be moved into itself.")
        val target = uniqueTarget(destDir, src.name)
        if (src.renameTo(target)) return@runCatching target
        copyTo(src, target)
        if (!src.deleteRecursively()) throw IllegalStateException("Copied, but the original could not be removed.")
        target
    }

    fun paste(destDir: File): Result<File?> {
        val path = FileClipboard.path ?: return Result.success(null)
        val cut = FileClipboard.cut
        val src = File(path)
        if (!src.exists()) {
            FileClipboard.clear()
            return Result.failure(FileNotFoundException("The copied item no longer exists."))
        }
        val result = if (cut) move(src, destDir) else copy(src, destDir)
        if (result.isSuccess && cut) FileClipboard.clear()
        return result
    }

    private fun copyTo(src: File, target: File) {
        if (src.isDirectory) {
            if (!src.copyRecursively(target, overwrite = false)) {
                throw IllegalStateException("The folder could not be copied.")
            }
        } else {
            src.copyTo(target, overwrite = false)
        }
    }

    private fun isInside(src: File, destDir: File): Boolean {
        val source = src.absoluteFile.toPath().normalize().toString()
        val dest = destDir.absoluteFile.toPath().normalize().toString()
        return dest == source || dest.startsWith("$source/")
    }

    private fun uniqueTarget(dir: File, name: String): File {
        var target = File(dir, name)
        if (!target.exists()) return target
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var index = 1
        while (target.exists()) {
            target = File(dir, "$base ($index)$ext")
            index++
        }
        return target
    }

    private fun problemWith(name: String): String? = when {
        name.isBlank() -> "The name is empty."
        name == "." || name == ".." -> "That name is not allowed."
        name.contains('/') || name.contains('\\') -> "The name cannot contain a path separator."
        name.length > 240 -> "The name is too long."
        else -> null
    }
}
