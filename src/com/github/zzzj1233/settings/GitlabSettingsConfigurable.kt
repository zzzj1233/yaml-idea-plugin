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
        return settingsComponent.commonModuleText.text != settings.commonModuleName
    }

    override fun apply() {
        val settings: GitlabSettingState = GitlabSettingState.getInstance()
        settings.commonModuleName = settingsComponent.commonModuleText.text
    }

    override fun reset() {
        val settings: GitlabSettingState = GitlabSettingState.getInstance()
        settingsComponent.commonModuleText.text = settings.commonModuleName
    }

}
