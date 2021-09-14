package com.github.zzzj1233.gitlab

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.zzzj1233.model.GitBranch
import com.github.zzzj1233.settings.GitlabSettingState
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

object GitlabSdk {

    private val objectMapper = ObjectMapper()

    private const val GIT_LABEL_TOKEN_KEY = "PRIVATE-TOKEN"

    fun branches(): List<GitBranch> {
        val settings = GitlabSettingState.getInstance()

        val httpClient = HttpClientBuilder.create().build()

        httpClient.use {
            val httpGet = HttpGet("${settings.uri}/api/v4/projects/${settings.projectId}/repository/branches")
            httpGet.addHeader(GIT_LABEL_TOKEN_KEY, settings.accessKey)
            it.execute(httpGet).use { response ->
                val json = EntityUtils.toString(response.entity)
                return objectMapper.readValue(json, object : TypeReference<List<GitBranch>>() {})
                        .sortedByDescending { branch -> branch.commit?.committed_date }
            }
        }
    }

    fun yamlContent(branchName: String) {
        val settings = GitlabSettingState.getInstance()

        val uri = "${settings.uri}/api/v4/projects/${settings.projectId}/repository/files//raw?ref=${branchName}"


    }

}