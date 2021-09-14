package com.github.zzzj1233.util

import com.intellij.openapi.vfs.VirtualFile
import java.io.InputStreamReader

fun VirtualFile?.walkFilter(predicate: (VirtualFile) -> Boolean): List<VirtualFile> {
    if (this == null || !this.isDirectory) {
        if (this != null && predicate.invoke(this)) {
            return listOf(this)
        }
        return emptyList()
    }

    val result = mutableListOf<VirtualFile>()

    walkFilter(this, predicate, result)

    return result
}

private fun walkFilter(file: VirtualFile, predicate: (VirtualFile) -> Boolean, result: MutableList<VirtualFile>) {
    file.children?.filter {
        it.isDirectory
    }?.forEach {
        walkFilter(it, predicate, result)
    }

    file.children
            .filterNotNull()
            .filter {
                !it.isDirectory
            }.filter {
                predicate.invoke(it)
            }.forEach {
                result.add(it)
            }
}

@Throws(RuntimeException::class)
fun Process.smartExecute(): List<String> {
    val code = this.waitFor()

    if (code != 0) {
        val msg = InputStreamReader(this.errorStream).use { it.readText() }
        throw RuntimeException(msg)
    }

    return InputStreamReader(this.inputStream).use {
        it.readLines()
    }
}

