package com.github.zzzj1233.model

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.contents.DocumentContent
import com.intellij.openapi.fileTypes.FileTypeManager
import org.yaml.snakeyaml.Yaml

data class YamlDiffHolder(val moduleName: String, private val diff: Pair<Map<String, Any>, Map<String, Any>>, val warning: Boolean = false) {

    val before: DocumentContent

    val after: DocumentContent

    init {
        before = mapToDiffContent(diff.first)
        after = mapToDiffContent(diff.second)
    }

    companion object {
        private val YAML_FILE_TYPE = FileTypeManager.getInstance().getFileTypeByExtension("YAML");

        private fun mapToDiffContent(map: Map<String, Any>): DocumentContent {
            val text = Yaml().dumpAsMap(map)
            return DiffContentFactory.getInstance().create(text, YAML_FILE_TYPE)
        }
    }

}