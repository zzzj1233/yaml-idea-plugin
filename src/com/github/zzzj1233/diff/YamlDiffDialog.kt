package com.github.zzzj1233.diff

import com.github.zzzj1233.model.YamlDiffHolder
import com.intellij.diff.DiffContext
import com.intellij.diff.contents.DiffContent
import com.intellij.diff.requests.ContentDiffRequest
import com.intellij.diff.tools.simple.SimpleDiffViewer
import com.intellij.diff.tools.util.base.HighlightPolicy
import com.intellij.diff.tools.util.base.IgnorePolicy
import com.intellij.diff.tools.util.base.TextDiffSettingsHolder.TextDiffSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.Dimension
import javax.swing.JComponent

class YamlDiffDialog(private val project: Project, private val diffHolder: YamlDiffHolder) : DialogWrapper(project) {

    init {
        this.title = diffHolder.moduleName
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val ctx = object : DiffContext() {

            init {
                val settings = TextDiffSettings()
                settings.highlightPolicy = HighlightPolicy.BY_WORD
                settings.ignorePolicy = IgnorePolicy.IGNORE_WHITESPACES
                settings.contextRange = 2
                settings.isExpandByDefault = false
                putUserData(TextDiffSettings.KEY, settings)
            }

            override fun isWindowFocused() = false

            override fun isFocusedInWindow() = false

            override fun requestFocusInWindow() {

            }

            override fun getProject(): Nothing? = null
        }

        val diffRequest = object : ContentDiffRequest() {

            override fun getContents(): List<DiffContent> {
                return listOf(diffHolder.before, diffHolder.after)
            }

            override fun getContentTitles(): List<String> {
                return listOf("BeforeCommit", "AfterCommit")
            }

            override fun getTitle() = diffHolder.moduleName
        }

        val viewer = SimpleDiffViewer(ctx, diffRequest)

        viewer.init()
        viewer.component.preferredSize = Dimension(1500, 750)

        return viewer.component
    }


}