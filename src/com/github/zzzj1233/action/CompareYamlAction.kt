package com.github.zzzj1233.action

import com.github.zzzj1233.gitlab.GitlabSdk
import com.github.zzzj1233.model.GitBranch
import com.github.zzzj1233.settings.GitlabSettingState
import com.github.zzzj1233.util.BalloonNotifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.popup.list.ListPopupImpl

class CompareYamlAction : AnAction() {

    companion object {
        private val log = Logger.getInstance(CompareYamlAction::class.java)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val settings = GitlabSettingState.getInstance()
        val project = event.project

        try {
            // 1. 从gitlab拉取分支
            val branches = GitlabSdk.branches()

            val popupStep = object : BaseListPopupStep<String>("Branches", branches.map { it.name }) {
                override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                    selectedValue?.apply { onChooseBranch(this, branches.map { it.name!! to it }.toMap()) }
                    return super.onChosen(selectedValue, finalChoice)
                }
            }

            // 2. 显示pop
            ListPopupImpl(project, popupStep).showCenteredInCurrentWindow(project
                    ?: ProjectManager.getInstance().defaultProject)

        } catch (e: Exception) {
            BalloonNotifications.showErrorNotification("从Gitlab拉取分支失败,请检测Gitlab配置")
            log.error("fetch branches from gitlab failed settings = $settings , error:", e)
        }

    }

    // 选择了要比较的分支
    fun onChooseBranch(branchName: String, branches: Map<String, GitBranch>) {
        val settings = GitlabSettingState.getInstance()
        // 1. 比较lastCommitId,如果不相同,则拉取最新的文件,以及保存为最新的commitId
        val lastCommitId = branches[branchName]?.commit?.id

        if (lastCommitId == null) {
            log.error("lastCommitId is null ? , branch = $branchName")
        }

        // 2. 使用缓存的文件内容
        if (true == settings.branches[branchName]?.equals(lastCommitId)) {

        }

        // 3. 使用sdk获取最新的文件内容

    }

}