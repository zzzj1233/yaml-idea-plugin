package com.github.zzzj1233.action

import com.github.zzzj1233.settings.GitlabSettingState
import com.github.zzzj1233.util.BalloonNotifications
import com.github.zzzj1233.util.GitUtil
import com.github.zzzj1233.util.MapUtils
import com.github.zzzj1233.util.YamlUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.popup.list.ListPopupImpl
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader

class CompareYamlAction : AnAction() {

    companion object {
        private val log = logger<CompareYamlAction>()
        private val regex = ".*/application(-(dev|fat|uat|prod|common|commondev|commonfat|commonuat|commonprod))?\\.ya?ml\$".toRegex()
    }

    override fun actionPerformed(event: AnActionEvent) {
        val settings = GitlabSettingState.getInstance()
        val project = event.project

        if (project == null || project.basePath.isNullOrBlank()) {
            BalloonNotifications.showErrorNotification("获取不到当前所在项目")
            return
        }

        val workdir: String = project.basePath!!

        // 1. 获取git分支
        val branches = GitUtil.branches(workdir)

        val popupStep = object : BaseListPopupStep<String>("Branches", branches) {
            override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                selectedValue?.apply { onChooseBranch(workdir, this) }
                return super.onChosen(selectedValue, finalChoice)
            }
        }

        // 2. 显示popup
        ListPopupImpl(project, popupStep).showCenteredInCurrentWindow(project)
    }

    // 选择了要比较的分支
    fun onChooseBranch(workdir: String, compareBranch: String) {
        val diffStat = GitUtil.diffStat(workdir, compareBranch)

        if (diffStat.isEmpty()) {
            return
        }

        var yamlDiffList = diffStat.filter {
            regex.matches(it)
        }

        if (yamlDiffList.isEmpty()) {
            return BalloonNotifications.showSuccessNotification("当前版本和${compareBranch}的yml文件没有差异")
        }

        // 那些模块的yaml做了变更?
        var modules = yamlDiffList.map {
            it.substringBefore("/")
        }

        // 忽略的模块
        val ignoreModules = modules.filter { !it.startsWith("goldhorse") }.distinct()

        ignoreModules.let {
            if (it.isNotEmpty())
                BalloonNotifications.showSuccessNotification("这些模块 [${ignoreModules.joinToString(",")}] 将会被忽略比较")
        }

        modules = modules.filter { it.startsWith("goldhorse") }
        yamlDiffList = yamlDiffList.filter { it.startsWith("goldhorse") }

        val settingState = GitlabSettingState.getInstance()

        // common模块发生了变更
        if (modules.contains(settingState.commonModuleName)) {

            // common模块
            val commonModules = yamlDiffList.filter {
                it.startsWith(settingState.commonModuleName ?: GitlabSettingState.DEFAULT_MODULE_NAME)
            }

            if (commonModules.size > 5) {
                log.warn("commonModules.size > 5 ? commonModules = ${commonModules.joinToString()}")
                return BalloonNotifications.showErrorNotification("无法进行比较! ${settingState.commonModuleName}存在了相同名称的配置文件")
            }

            val commonChanged = commonModules.any { it.endsWith("common.yml") || it.endsWith("common.yaml") }

        }

        // 根据module分组
        val moduleDiffMap: Map<String, List<String>> = yamlDiffList
                .groupBy { it.substringBefore("/") }
                .filter { it.key != settingState.commonModuleName }

        for ((moduleName, yamlDiffFiles) in moduleDiffMap) {
            // application.yml是否发生了变化?
            val commonDiffYml = yamlDiffFiles.filter { it.contains("application.yml") || it.contains("application.yaml") }

            if (commonDiffYml.size > 1) {
                BalloonNotifications.showErrorNotification("${moduleName}存在多个`application.ya?ml`文件?,该模块将会被忽略比较")
                continue
            }

            // 当前的application.yml文件
            val commonYamlMap = File("$workdir/$moduleName/src/main/resources/application.yml").run {
                if (this.exists()) YamlUtils.toMap(FileReader(this).use { it.readText() })
                else YamlUtils.toMap(FileReader(File("$workdir/$moduleName/src/main/resources/application.yaml")).use { it.readText() })
            }

            // diff -stat包含application.yml
            val branchCommonYamlMap = if (commonDiffYml.isNotEmpty()) {
                YamlUtils.toMap(GitUtil.fileContent(workdir, compareBranch, commonDiffYml.first()))
            } else {
                commonYamlMap
            }

            yamlDiffFiles.filter { !it.contains("application.yml") && !it.contains("application.yaml") }
                    .forEach { diffYamlFilePath ->
                        // 找文件系统的文件
                        val localYamlFile = File("$workdir/$diffYamlFilePath").run {
                            if (this.exists()) this else null
                        }

                        val localYamlText = localYamlFile?.inputStream()?.run {
                            InputStreamReader(this)
                                    .use {
                                        it.readText()
                                    }
                        }

                        val localYamlMap = YamlUtils.toMap(localYamlText)
                        val branchYamlMap = YamlUtils.toMap(GitUtil.fileContent(workdir, compareBranch, diffYamlFilePath))

                        val diff = MapUtils.compareMap(branchYamlMap, localYamlMap)

                        println("$moduleName ---> $diff")
                    }
        }

    }

}