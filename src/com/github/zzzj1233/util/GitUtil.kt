package com.github.zzzj1233.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.rt.execution.testFrameworks.ProcessBuilder
import java.io.File

object GitUtil {

    private val log = Logger.getInstance(GitUtil::class.java)

    fun branches(workdir: String): List<String> {
        val builder = ProcessBuilder()
        builder.add("git")
        builder.add("branch")
        builder.add("--sort=-committerdate")
        builder.setWorkingDir(File(workdir))


        return try {
            builder.createProcess().execute()
        } catch (e: Exception) {
            BalloonNotifications.showErrorNotification("执行git branch命令失败 , cause : ${e.message}")
            emptyList()
        }
    }

    fun diffStat(workdir: String, targetBranch: String? = null, currentBranch: String? = null): List<String> {
        val builder = ProcessBuilder()
        builder.add("git")
        builder.add("diff")
        currentBranch?.apply { builder.add(this.trim()) }
        targetBranch?.apply { builder.add(this.trim()) }
        builder.add("--name-only")
        builder.setWorkingDir(File(workdir))



        return try {
            builder.createProcess().execute()
        } catch (e: Exception) {
            BalloonNotifications.showErrorNotification("执行git diff命令失败 , cause : ${e.message}")
            emptyList()
        }
    }

    fun fileContent(workdir: String, branch: String, filePath: String): String? {
        val builder = ProcessBuilder()
        builder.add("git")
        builder.add("show")
        builder.add("${branch.trim()}:${filePath.trim()}")
        builder.setWorkingDir(File(workdir))

        return try {
            builder.createProcess().execute().joinToString(System.lineSeparator())
        } catch (e: Exception) {
            BalloonNotifications.showErrorNotification("执行git show命令失败 , cause : ${e.message}")
            null
        }
    }

}