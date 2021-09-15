package com.github.zzzj1233.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import java.io.InputStreamReader
import java.util.*
import kotlin.system.measureTimeMillis

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
fun Process.execute(): List<String> {

    val scanner = Scanner(this.inputStream)

    val future = ApplicationManager.getApplication().executeOnPooledThread<List<String>> {
        val list = mutableListOf<String>()

        scanner.use {
            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine())
            }
            list
        }
    }

    val code = this.waitFor()

    if (code != 0) {
        val msg = InputStreamReader(this.errorStream).use { it.readText() }
        throw RuntimeException(msg)
    }

    return future.get()

}

