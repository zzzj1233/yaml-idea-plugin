package com.github.zzzj1233.model

import com.github.zzzj1233.util.MapUtils
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.contents.DiffContent
import com.intellij.diff.contents.DocumentContent
import com.intellij.openapi.fileTypes.FileTypeManager
import org.yaml.snakeyaml.Yaml

data class YamlDiffHolder(val moduleName: String, private val diff: () -> Pair<MutableMap<String, Any>, MutableMap<String, Any>>) {

    val diffContents: Pair<DocumentContent, DocumentContent> by lazy {
        val diffPair = diff()
        mapToDiffContent(diffPair.first) to mapToDiffContent(diffPair.second)
    }

    val warning: Boolean by lazy {
        false
    }

    companion object {
        private val YAML_FILE_TYPE = FileTypeManager.getInstance().getFileTypeByExtension("YAML");

        private fun mapToDiffContent(map: MutableMap<String, Any>): DocumentContent {
            val text = if (map.isEmpty()) {
                ""
            } else {
                MapUtils.removeEmptyValues(map)
                if (map.isEmpty()) "" else Yaml().dumpAsMap(map)
            }
            return DiffContentFactory.getInstance().create(text, YAML_FILE_TYPE)
        }
    }

}