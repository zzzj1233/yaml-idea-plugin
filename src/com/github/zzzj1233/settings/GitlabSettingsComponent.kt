package com.github.zzzj1233.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class GitlabSettingsComponent {

    val mainPanel: JPanel

    val uriText = JBTextField()

    val projectIdText = JBTextField()

    val accessKeyText = JBTextField()

    fun getPreferredFocusedComponent(): JComponent? {
        return uriText
    }

    init {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Gitlab uri "), uriText, 1, false)
                .addLabeledComponent(JBLabel("Project id "), projectIdText, 1, false)
                .addLabeledComponent(JBLabel("Access key "), accessKeyText, 1, false)
                .addComponentFillVertically(JPanel(), 0)
                .panel
    }

}