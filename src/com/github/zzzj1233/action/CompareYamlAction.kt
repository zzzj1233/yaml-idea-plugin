package com.github.zzzj1233.action

import com.github.zzzj1233.diff.YamlTableDialog
import com.github.zzzj1233.model.YamlDiffContext
import com.github.zzzj1233.model.YamlDiffHolder
import com.github.zzzj1233.util.*
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.popup.list.ListPopupImpl
import java.nio.charset.StandardCharsets

class CompareYamlAction : AnAction() {

    companion object {
        private val log = logger<CompareYamlAction>()
        val regex = ".*/application(-(dev|fat|uat|prod|common|commondev|commonfat|commonuat|commonprod))?\\.ya?ml\$".toRegex()
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getRequiredData(CommonDataKeys.PROJECT)

        val workdir: String = project.basePath!!

        // 1. 获取git分支
        val branches = GitUtil.branches(workdir)

        val popupStep = object : BaseListPopupStep<String>("Branches", branches) {
            var selectedValue: String? = null

            override fun getFinalRunnable(): Runnable? {
                return Runnable {
                    selectedValue?.let { onChooseBranch(workdir, it, project) }
                }
            }

            override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                this.selectedValue = selectedValue
                return super.onChosen(selectedValue, finalChoice)
            }
        }

        // 2. 显示popup
        ListPopupImpl(project, popupStep).showCenteredInCurrentWindow(project)
    }

    // 选择了要比较的分支
    fun onChooseBranch(workdir: String, compareBranch: String, project: Project) {

        val diffStat = GitUtil.diffStat(workdir, compareBranch)

        if (diffStat.isEmpty()) {
            return YamlTableDialog(project, YamlDiffContext(), okText = "Ok", dialogTitle = "Compare with ${compareBranch.trim()}", leftPanelTitle = compareBranch, rightPanelTitle = "Local").show()
        }

        // 用正则筛选出所有的yaml配置文件
        var yamlDiffList = diffStat.filter {
            regex.matches(it)
        }

        if (yamlDiffList.isEmpty()) {
            return YamlTableDialog(project, YamlDiffContext(), okText = "Ok", dialogTitle = "Compare with ${compareBranch.trim()}", leftPanelTitle = compareBranch, rightPanelTitle = "Local").show()
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

        yamlDiffList = yamlDiffList.filter { it.startsWith("goldhorse") }

        val moduleDiffMap: Map<String, List<String>> = yamlDiffList
                .groupBy { it.substringBefore("/") }

        val gitYamlDiff = YamlDiffContext()

        val compareYamlFun = genCompareYamlFun(workdir, compareBranch)

        val commonYamlMap: MutableMap<String, String> = mutableMapOf()

        for ((moduleName, diffList) in moduleDiffMap) {

            val common = diffList.find { it.findYaml("application", "application-common") }

            common?.let {
                commonYamlMap[moduleName] = it
            }

            diffList.find { it.findYaml("application-dev", "application-commondev") }?.let {
                gitYamlDiff.addDevModule(moduleName, YamlDiffHolder(moduleName, compareYamlFun.invoke(it, commonYamlMap[moduleName])))
            }

            diffList.find { it.findYaml("application-fat", "application-commonfat") }?.let {
                gitYamlDiff.addFatModule(moduleName,
                        YamlDiffHolder(moduleName, compareYamlFun.invoke(it, commonYamlMap[moduleName])))
            }

            diffList.find { it.findYaml("application-uat", "application-commonuat") }?.let {
                gitYamlDiff.addUatModule(moduleName, YamlDiffHolder(moduleName, compareYamlFun.invoke(it, commonYamlMap[moduleName])))
            }

            diffList.find { it.findYaml("application-prod", "application-commonprod") }?.let {
                gitYamlDiff.addProdModule(moduleName, YamlDiffHolder(moduleName, compareYamlFun.invoke(it, commonYamlMap[moduleName])))
            }
        }

        commonYamlMap.forEach {
            gitYamlDiff.addModuleIfAbsent(it.key, YamlDiffHolder(it.key, compareYamlFun.invoke(it.value, null)))
        }

        YamlTableDialog(project, gitYamlDiff, okText = "Ok", dialogTitle = "Compare with ${compareBranch.trim()}", leftPanelTitle = compareBranch, rightPanelTitle = "Local").show()
    }

    private fun genCompareYamlFun(workdir: String, branchName: String): (String, String?) -> () -> Pair<MutableMap<String, Any>, MutableMap<String, Any>> {
        return { yamlFilePath, commonYamlFilePath ->
            {
                val path = "$workdir/$yamlFilePath"

                val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$path")

                val commonYamlMap = commonYamlFilePath?.run {
                    val text = VirtualFileManager.getInstance().findFileByUrl("file://$workdir/$this")?.contentsToByteArray()?.toString(StandardCharsets.UTF_8)
                    YamlUtils.toMap(text)
                }

                val localYamlMap = YamlUtils.toMap(virtualFile?.contentsToByteArray()?.toString(StandardCharsets.UTF_8))
                val branchYamlMap = YamlUtils.toMap(GitUtil.fileContent(workdir, branchName, yamlFilePath))

                commonYamlMap?.let {
                    MapUtils.override(it, localYamlMap)
                    MapUtils.override(it, branchYamlMap)
                }

                MapUtils.compareMap(branchYamlMap, localYamlMap)
            }
        }
    }

}