package com.github.zzzj1233.util

import org.yaml.snakeyaml.Yaml

object YamlUtils {

    fun toMap(content: String?): Map<String, Any> =
            if (content != null) Yaml().loadAs(content, Map::class.java) as Map<String, Any>
            else emptyMap()

}


