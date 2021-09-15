package com.github.zzzj1233.util

import java.io.File

object FileUtils {

    fun findByExtension(path: String, vararg extensions: String): File? {
        return extensions.map { File("$path$it") }.find { it.exists() }
    }

}