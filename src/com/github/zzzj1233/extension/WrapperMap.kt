package com.github.zzzj1233.extension

/**
 * 优先从当前map中获取值,如果当前map没有,那么从备选拿
 */
class WrapperMap(private val origin: Map<String, Any>, private val alternative: Map<String, Any>) : Map<String, Any> by origin {

    override fun get(key: String): Any? {
        val value = origin[key]

        if (value != null && value is Map<*, *> && value !is WrapperMap) {
            if (alternative[key] is Map<*, *>) {
                return WrapperMap(value as Map<String, Any>, alternative[key] as Map<String, Any>)
            }
        }

        return value ?: alternative[key]
    }

    override fun getOrDefault(key: String, defaultValue: Any): Any {
        val value = origin[key]

        if (value != null && value is Map<*, *> && value !is WrapperMap) {
            if (alternative[key] is Map<*, *>) {
                return WrapperMap(value as Map<String, Any>, alternative[key] as Map<String, Any>)
            }
        }

        return value ?: alternative.getOrDefault(key, defaultValue)
    }

    fun getByPath(path: String): Any? {
        val props = path.split(".")

        var result: Any? = null

        var map: Map<String, Any> = this

        for (prop in props) {
            result = map[prop]
            if (result == null || result !is Map<*, *>) {
                return result
            }
            map = result as Map<String, Any>
        }

        return result
    }

}