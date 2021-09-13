package com.github.zzzj1233.util

import org.yaml.snakeyaml.Yaml

object YamlUtils {

    fun toMap(content: String?): MutableMap<String, Any> =
            if (content.isNullOrBlank()) mutableMapOf()
            else Yaml().loadAs(content, Map::class.java) as MutableMap<String, Any>

}


