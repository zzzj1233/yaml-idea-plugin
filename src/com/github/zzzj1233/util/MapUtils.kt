package com.github.zzzj1233.util

import kotlin.test.assertNotEquals

/**
 * @author Zzzj
 * @create 2021-09-09 22:34
 */

object MapUtils {

    // 使用高优先级的map覆盖低优先级的map
    fun override(low: Map<String, Any>, high: MutableMap<String, Any>) {
        (low.keys - high.keys).forEach {
            low[it]?.run { high[it] = this }
        }

        high.keys.forEach {
            val highValue = high[it]
            val lowValue = low[it]
            if (highValue != null && highValue is Map<*, *> && lowValue != null && lowValue is Map<*, *>) {
                override(lowValue as Map<String, Any>, highValue as MutableMap<String, Any>)
            }
        }
    }

    fun compareMap(old: Map<String, Any>, new: Map<String, Any>): Pair<Map<String, Any>, Map<String, Any>> {
        val oldMap = mutableMapOf<String, Any>()
        val newMap = mutableMapOf<String, Any>()

        val result = oldMap to newMap

        compareMap(old, new, oldMap, newMap)

        return result
    }

    private fun compareMap(old: Map<String, Any>,
                           new: Map<String, Any>,
                           oldMap: MutableMap<String, Any>,
                           newMap: MutableMap<String, Any>
    ) {
        val oldKeys = old.keys
        val newKeys = new.keys


        (oldKeys - newKeys).forEach {
            old[it]?.run {
                oldMap[it] = this
            }
        }

        (newKeys - oldKeys).forEach {
            new[it]?.run {
                newMap[it] = this
            }
        }

        (oldKeys - (oldKeys - newKeys)).forEach {
            if (old[it] == null && new[it] == null) {
                return@forEach
            } else if (old[it] == new[it]) {
                return@forEach
            } else if (old[it] == null) {
                newMap[it] = new[it]!!
            } else if (new[it] == null) {
                oldMap[it] = old[it]!!
            } else if (old[it] is Map<*, *> && new[it] is Map<*, *>) {
                val oldNestMap = mutableMapOf<String, Any>()
                val newNestMap = mutableMapOf<String, Any>()

                oldMap[it] = oldNestMap
                newMap[it] = newNestMap

                compareMap(old[it] as Map<String, Any>, new[it] as Map<String, Any>, oldNestMap, newNestMap)
            } else {
                oldMap[it] = old[it]!!
                newMap[it] = new[it]!!
            }
        }
    }


}
