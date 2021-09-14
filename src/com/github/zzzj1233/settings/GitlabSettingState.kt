package com.github.zzzj1233.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "GitlabSettingState", storages = [Storage(file = "yaml_gitlab.xml")])
class GitlabSettingState : PersistentStateComponent<GitlabSettingState> {

    var uri: String? = null
        get() = field ?: DEFAULT_URI

    var projectId: Int? = null
        get() = field ?: DEFAULT_PROJECT_ID

    var accessKey: String? = null
        get() = field ?: DEFAULT_ACCESS_KEY

    var branches: MutableMap<String, Long> = mutableMapOf()

    companion object {
        fun getInstance(): GitlabSettingState = ServiceManager.getService(GitlabSettingState::class.java)

        const val DEFAULT_URI = "http://172.16.50.164"

        const val DEFAULT_PROJECT_ID = 117

        const val DEFAULT_ACCESS_KEY = "qdNwxHSc8ixwoMBxhPMv"
    }

    override fun getState(): GitlabSettingState? {
        return this
    }

    override fun loadState(state: GitlabSettingState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun toString(): String {
        return "GitlabSettingState(uri = $uri, projectId = $projectId, accessKey = $accessKey)"
    }


}