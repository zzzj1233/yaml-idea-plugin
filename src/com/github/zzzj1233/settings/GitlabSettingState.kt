package com.github.zzzj1233.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "GitlabSettingState", storages = [Storage(file = "yaml_gitlab.xml")])
class GitlabSettingState : PersistentStateComponent<GitlabSettingState> {

    var commonModuleName: String? = null
        get() = field ?: DEFAULT_MODULE_NAME

    var branches: MutableMap<String, Long> = mutableMapOf()

    companion object {
        fun getInstance(): GitlabSettingState = ServiceManager.getService(GitlabSettingState::class.java)

        const val DEFAULT_MODULE_NAME = "goldhorse-common"
    }

    override fun getState(): GitlabSettingState? {
        return this
    }

    override fun loadState(state: GitlabSettingState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun toString(): String {
        return "GitlabSettingState(commonModuleName = $commonModuleName)"
    }

}