package com.github.zzzj1233.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "YamlCheckBoxConfig", storages = [Storage(file = "yamlbox_info.xml")])
class YamlCheckBoxState : PersistentStateComponent<YamlCheckBoxState> {

    var open = true

    override fun getState(): YamlCheckBoxState? {
        return this
    }

    override fun loadState(state: YamlCheckBoxState) {
        XmlSerializerUtil.copyBean(state, this)
    }

}