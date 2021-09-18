package com.github.zzzj1233.model

import com.github.zzzj1233.enums.GhEnv

class YamlDiffContext {

    val devModules: MutableMap<String, YamlDiffHolder> = mutableMapOf()

    val fatModules: MutableMap<String, YamlDiffHolder> = mutableMapOf()

    val uatModules: MutableMap<String, YamlDiffHolder> = mutableMapOf()

    val prodModules: MutableMap<String, YamlDiffHolder> = mutableMapOf()

    operator fun get(env: GhEnv): MutableMap<String, YamlDiffHolder> {
        return when (env) {
            GhEnv.DEV -> devModules
            GhEnv.FAT -> fatModules
            GhEnv.UAT -> uatModules
            GhEnv.PROD -> prodModules
        }
    }

    fun addDevModule(module: String, yamlDiffHolder: YamlDiffHolder) {
        devModules[module] = yamlDiffHolder
    }

    fun addFatModule(module: String, yamlDiffHolder: YamlDiffHolder) {
        fatModules[module] = yamlDiffHolder
    }

    fun addUatModule(module: String, yamlDiffHolder: YamlDiffHolder) {
        uatModules[module] = yamlDiffHolder
    }

    fun addProdModule(module: String, yamlDiffHolder: YamlDiffHolder) {
        prodModules[module] = yamlDiffHolder
    }

    fun addModuleIfAbsent(module: String, yamlDiffHolder: YamlDiffHolder) {
        devModules.putIfAbsent(module, yamlDiffHolder)
        fatModules.putIfAbsent(module, yamlDiffHolder)
        uatModules.putIfAbsent(module, yamlDiffHolder)
        prodModules.putIfAbsent(module, yamlDiffHolder)
    }

}