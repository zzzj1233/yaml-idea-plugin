package com.github.zzzj1233.checkin

import com.github.zzzj1233.extension.WrapperMap
import com.github.zzzj1233.util.BalloonNotifications
import com.github.zzzj1233.util.YamlUtils
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption
import com.intellij.openapi.vcs.checkin.CheckinHandler
import java.util.function.Consumer

class YamlCheckInHandler(val panel: CheckinProjectPanel, val ctx: CommitContext) : CheckinHandler() {

    private var open: Boolean = true

    override fun getBeforeCheckinConfigurationPanel() = BooleanCommitOption(panel, "Yaml check", true, this::open,
            Consumer { open = it }
    )

    companion object {
        private const val COMMON_MODULE_NAME = "goldhorse-common";

        private val prod = arrayOf("application", "application-prod")
        private val commonProd = arrayOf("application-common", "application-common-prod")

        private val LOG = Logger.getInstance(FileChooser::class.java)

        val prodConfigs = prod.map { it.plus(".yml") } + prod.map { it.plus(".yaml") }
        val commonProdConfigs = commonProd.map { it.plus(".yml") } + prod.map { it.plus(".yaml") }
    }

    override fun beforeCheckin(): ReturnResult {
        // 1. 没有勾选yaml check
        if (!open) {
            return ReturnResult.COMMIT
        }

        // YamlDiffDialog(panel.project).show()
        val changes = panel.selectedChanges

        val prodChanges = changes.filter { prodConfigs.contains(it.virtualFile?.name) }
        val commonProdChanges = changes.filter { commonProdConfigs.contains(it.virtualFile?.name) }.toMutableList()

        // 2. 没有检查到prod的改变
        if (prodChanges.isEmpty() && commonProdChanges.isEmpty()) {
            return ReturnResult.COMMIT
        }

        if (commonProdChanges.isNotEmpty()) {
            // 3. application-common 和 application-commonprod只允许存在于goldhorse-common这个模块中
            changes.filter { change -> change.virtualFile == null }.also {
                if (it.isNotEmpty()) {
                    LOG.error("changes virtualFile is null ? , changes = {} ", it.toString())
                }
            }

            changes.mapNotNull { it.virtualFile }
                    .mapNotNull { ModuleUtil.findModuleForFile(it, panel.project) }
                    .map { it.name }
                    .filter { it != COMMON_MODULE_NAME }
                    .let {
                        BalloonNotifications.showWarningNotification("检测到${it.joinToString(",")}包含${commonProd}配置文件,这些文件将被忽略检查")
                    }
        }


        // 4. 如果commonProdChanges被清空了,那么直接commit
        if (prodChanges.isEmpty() && commonProdChanges.isEmpty()) {
            return ReturnResult.COMMIT
        }

        // 5. 只有commonProd的配置发生了变更
        if (prodChanges.isEmpty()) {
            val model = "Global"

            if (prodChanges.size != 2) {
                BalloonNotifications.showWarningNotification("检测到多个common配置文件${prodChanges.map { it.virtualFile?.name }.joinToString(",")},这些文件将被忽略检查")
                return ReturnResult.COMMIT
            }

            // 合并common.yml和commonprod.yml
            var common = prodChanges.find { change -> change.virtualFile!!.name.contains("prod") }!!
            var prod = prodChanges.find { change -> !change.virtualFile!!.name.contains("prod") }!!

            val beforeYamlMap = WrapperMap(YamlUtils.toMap(prod.beforeRevision?.content), YamlUtils.toMap(common.beforeRevision?.content))
            val afterYamlMap = WrapperMap(YamlUtils.toMap(prod.afterRevision?.content), YamlUtils.toMap(common.afterRevision?.content))

            // 比较两个map的内容

        }

        // 6. 将application.yml, application-prod.yml, application-common.yml, application-commonprod.yml的配置合并


        return ReturnResult.CANCEL
    }
}