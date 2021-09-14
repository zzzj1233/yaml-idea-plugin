package com.github.zzzj1233.model

import  com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class GitBranch {

    var name: String? = null

    var commit: GitCommit? = null

}