package com.github.zzzj1233.model

import com.intellij.openapi.vcs.changes.Change

class ChangeWrapper(val change: Change) {

    val moduleName: String?

    val virtualFile = change.virtualFile!!

    init {
        val value = reg.matchEntire(change.virtualFile!!.path)?.groups?.get(1)?.value
        this.moduleName = if (value?.substringAfter("\\") == value) value?.substringAfter("/")
        else value?.substringAfter("\\")
    }

    companion object {
        val reg = ".+?(goldhorse-.+).+src.+".toRegex()
    }

}