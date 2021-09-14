package com.github.zzzj1233.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.rt.execution.testFrameworks.ProcessBuilder

object GitUtil {

    private val log = Logger.getInstance(GitUtil::class.java)

    fun branches(): List<String> {
        val builder = ProcessBuilder()
        builder.add("git")
        builder.add("branch")
        builder.add("--sort=-committerdate")

        val process = builder.createProcess()

        return process.smartExecute()
    }

    fun diffStat(targetBranch: String? = null, currentBranch: String? = null): List<String> {
        val builder = ProcessBuilder()
        builder.add("git")
        builder.add("diff")
        currentBranch?.apply { builder.add(this) }
        targetBranch?.apply { builder.add(this) }
        builder.add("--name-only")

        val process = builder.createProcess()

        return process.smartExecute()
    }

    fun fileContent(branch: String, filePath: String): String {
        val builder = ProcessBuilder()
        builder.add("git")
        builder.add("show")
        builder.add("$branch:$filePath")

        val process = builder.createProcess()

        return process.smartExecute().joinToString()
    }

}