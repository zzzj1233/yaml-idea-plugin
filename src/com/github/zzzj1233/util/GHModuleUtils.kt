package com.github.zzzj1233.util

import com.github.zzzj1233.settings.GitlabSettingState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile

object GHModuleUtils {

    private val log = Logger.getInstance(GHModuleUtils::class.java)

    fun getModuleYamlFiles(project: Project?): Map<Module, List<VirtualFile>> {
        val settings = GitlabSettingState.getInstance()

        val modules = ModuleManager.getInstance(project ?: ProjectManager.getInstance().defaultProject).modules

        if (modules.none { it.name == settings.commonModuleName }) {
            BalloonNotifications.showErrorNotification("当前项目中未找到 [${settings.commonModuleName}] 这个模块,无法执行yaml检查")
            return emptyMap()
        }

        val moduleYamlMap = modules.mapNotNull {
            val resources = ModuleRootManager
                    .getInstance(it)
                    .sourceRoots
                    .filter { it.path.endsWith("/src/main/resources") }

            if (resources.size > 1) {
                BalloonNotifications.showWarningNotification("${it.name}包含多个/src/main/resources目录?该模板将被忽略检查")
                null
            } else {
                it!! to resources.getOrNull(0).walkFilter { resourceFile ->
                    resourceFile.name.startsWith("application") &&
                            (resourceFile.name.endsWith("yml") || resourceFile.name.endsWith("yaml"))
                }
            }
        }

        log.info("There are no yaml configuration files under these modules ${moduleYamlMap.filter {
            it.second.isEmpty()
        }.map { it.first.name }}")

        return moduleYamlMap.filter { it.second.isNotEmpty() }.toMap()
    }

}