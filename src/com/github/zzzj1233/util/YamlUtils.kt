package com.github.zzzj1233.util

import com.intellij.openapi.diagnostic.logger
import org.yaml.snakeyaml.Yaml

object YamlUtils {

    private val LOG = logger<YamlUtils>()

    fun toMap(content: String?): MutableMap<String, Any> {
        return if (content.isNullOrBlank()) {
            mutableMapOf()
        } else
            try {
                Yaml().loadAs(content, Map::class.java) as MutableMap<String, Any>
            } catch (e: Exception) {
                LOG.error("content to yaml error , content = $content")
                mutableMapOf<String, Any>()
            }
    }

}


