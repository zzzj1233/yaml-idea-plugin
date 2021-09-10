package com.github.zzzj1233.util

import com.intellij.diff.util.DiffUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vcs.changes.Change

fun yamlChanged(change: Change): Boolean {
    return change.virtualFile?.fileType?.defaultExtension.equals("yaml", true) ||
            change.virtualFile?.fileType?.defaultExtension.equals("yml", true) ||
            change.virtualFile?.name?.endsWith("yaml", true) ?: false ||
            change.virtualFile?.name?.endsWith("yml", true) ?: false
}

data class YamlConfigReviewable(
        val module: Module?, val key: String, val changeType: Change.Type,
        val beforeContent: String, val afterContent: String
) {
    companion object {
        fun fromChange(change: Change): Collection<YamlConfigReviewable> {
            // 1. 文件所属模块
            val module = change.virtualFile?.run { ModuleUtil.findModuleForFile(this, ProjectManager.getInstance().defaultProject) }

            // 2. 修改的key : compare yaml
            return emptyList()
        }
    }
}
