package com.github.zzzj1233

import com.github.zzzj1233.extension.WrapperMap
import com.github.zzzj1233.util.YamlUtils
import java.io.File

fun test() {

    val common = File(ClassLoader.getSystemClassLoader().getResource("common.yaml").toURI()).readText()
    val prod = File(ClassLoader.getSystemClassLoader().getResource("prod.yaml").toURI()).readText()

    val commonMap = YamlUtils.toMap(common)
    val prodMap = YamlUtils.toMap(prod)

    val map = WrapperMap(prodMap, commonMap)

    println(map.getByPath("goldhorse.env"))

}

fun main() {
    test()
}