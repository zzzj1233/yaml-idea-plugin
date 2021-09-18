package com.github.zzzj1233.util

import com.intellij.openapi.application.ApplicationManager
import java.io.InputStreamReader
import java.util.*

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

fun String.findYaml(vararg prefix: String) = prefix.any {
    this.endsWith("$it.yml") || this.endsWith("$it.yaml")
}