package com.github.zzzj1233.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class GitlabSettingsConfigurable : Configurable {

    var settingsComponent: GitlabSettingsComponent = GitlabSettingsComponent()

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String? {
        return "Yaml checker gitlab config"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return settingsComponent.getPreferredFocusedComponent()
    }

    override fun createComponent(): JComponent? {
        return settingsComponent.mainPanel
    }

    override fun isModified(): Boolean {
        val settings: GitlabSettingState = GitlabSettingState.getInstance()
        return settingsComponent.uriText.text != settings.uri ||
                settingsComponent.projectIdText.text != settings.projectId.toString() ||
                settingsComponent.accessKeyText.text != settings.accessKey
    }

    override fun apply() {
        val settings: GitlabSettingState = GitlabSettingState.getInstance()
        settings.uri = settingsComponent.uriText.text
        settings.projectId = settingsComponent.projectIdText.text.toInt()
        settings.accessKey = settingsComponent.accessKeyText.text
    }

    override fun reset() {
        val settings: GitlabSettingState = GitlabSettingState.getInstance()
        settingsComponent.uriText.text = settings.uri
        settingsComponent.projectIdText.text = settings.projectId.toString()
        settingsComponent.accessKeyText.text = settings.accessKey
    }

}
