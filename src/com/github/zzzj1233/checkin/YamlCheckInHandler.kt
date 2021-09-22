package com.github.zzzj1233.checkin

import com.github.zzzj1233.action.CompareYamlAction
import com.github.zzzj1233.config.YamlCheckBoxState
import com.github.zzzj1233.diff.YamlTableDialog
import com.github.zzzj1233.model.ChangeWrapper
import com.github.zzzj1233.model.YamlDiffContext
import com.github.zzzj1233.model.YamlDiffHolder
import com.github.zzzj1233.util.BalloonNotifications
import com.github.zzzj1233.util.MapUtils
import com.github.zzzj1233.util.YamlUtils
import com.github.zzzj1233.util.findYaml
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption
import com.intellij.openapi.vcs.checkin.CheckinHandler
import java.util.function.Consumer

class YamlCheckInHandler(val panel: CheckinProjectPanel, val ctx: CommitContext) : CheckinHandler() {

    private val state: YamlCheckBoxState = ServiceManager.getService(YamlCheckBoxState::class.java)

    private val project get() = panel.project

    override fun getBeforeCheckinConfigurationPanel() = BooleanCommitOption(panel, "Yaml check", true, state::open)

    companion object {
        private val LOG = logger<YamlCheckInHandler>()
    }

    override fun beforeCheckin(): ReturnResult {
        // 1. 没有勾选yaml check
        if (!state.open) {
            return ReturnResult.COMMIT
        }

        // YamlDiffDialog(panel.project).show()
        var changes = (panel.selectedChanges ?: emptyList())
                .filter { it.virtualFile != null }
                .filter { CompareYamlAction.regex.matches(it.virtualFile!!.path) }
                .map { ChangeWrapper(it) }
                .filter {
                    if (it.moduleName == null) {
                        LOG.warn("moduleName is null ? path = ${it.change.virtualFile!!.path}")
                        false
                    } else {
                        true
                    }
                }

        // 忽略的模块
        val ignoreModules = changes.mapNotNull { it.moduleName }.filter { !it.startsWith("goldhorse") }.distinct()

        ignoreModules.let {
            if (it.isNotEmpty())
                BalloonNotifications.showSuccessNotification("这些模块 [${ignoreModules.joinToString(",")}] 将会被忽略比较")
        }

        changes = changes.filter { !ignoreModules.contains(it.moduleName) }

        if (changes.isEmpty()) {
            return ReturnResult.COMMIT
        }

        val moduleDiffMap = changes.groupBy { it.moduleName!! }

        val yamlDiffContext = YamlDiffContext()

        val commonYamlMap = mutableMapOf<String, ChangeWrapper>()

        for ((moduleName, diffList) in moduleDiffMap) {

            val common = diffList.find { it.virtualFile.name.findYaml("application", "application-common") }

            common?.let {
                commonYamlMap[moduleName] = it
            }

            diffList.find { it.virtualFile.name.findYaml("application-dev", "application-commondev") }?.let {
                yamlDiffContext.addDevModule(moduleName, YamlDiffHolder(moduleName, genDiffFun(it, commonYamlMap[moduleName])))
            }

            diffList.find { it.virtualFile.name.findYaml("application-fat", "application-commonfat") }?.let {
                yamlDiffContext.addFatModule(moduleName,
                        YamlDiffHolder(moduleName, genDiffFun(it, commonYamlMap[moduleName])))
            }

            diffList.find { it.virtualFile.name.findYaml("application-uat", "application-commonuat") }?.let {
                yamlDiffContext.addUatModule(moduleName, YamlDiffHolder(moduleName, genDiffFun(it, commonYamlMap[moduleName])))
            }

            diffList.find { it.virtualFile.name.findYaml("application-prod", "application-commonprod") }?.let {
                yamlDiffContext.addProdModule(moduleName, YamlDiffHolder(moduleName, genDiffFun(it, commonYamlMap[moduleName])))
            }

        }

        commonYamlMap.forEach {
            yamlDiffContext.addModuleIfAbsent(it.key, YamlDiffHolder(it.key, genDiffFun(it.value, null)))
        }

        val yamlTableDialog = YamlTableDialog(project, yamlDiffContext)

        yamlTableDialog.show()

        return if (yamlTableDialog.commit) ReturnResult.COMMIT else ReturnResult.CANCEL
    }

    private fun genDiffFun(changeWrapper: ChangeWrapper, commonChange: ChangeWrapper?): () -> Pair<MutableMap<String, Any>, MutableMap<String, Any>> {
        val beforeCommonMap = YamlUtils.toMap(commonChange?.change?.beforeRevision?.content)
        val afterCommonMap = YamlUtils.toMap(commonChange?.change?.afterRevision?.content)

        val beforeMap = YamlUtils.toMap(changeWrapper.change.beforeRevision?.content)
        val afterMap = YamlUtils.toMap(changeWrapper.change.afterRevision?.content)

        MapUtils.override(beforeCommonMap, beforeMap)
        MapUtils.override(afterCommonMap, afterMap)

        return {
            MapUtils.compareMap(beforeMap, afterMap)
        }
    }

}