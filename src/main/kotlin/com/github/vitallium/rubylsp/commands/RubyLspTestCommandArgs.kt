package com.github.vitallium.rubylsp.commands

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

internal data class RubyLspTestCommandArgs(
    val path: String,
    val name: String?,
    val command: String?,
    val startLine: Int?,
    val startColumn: Int?
) {
    companion object {
        fun from(arguments: List<Any?>?): RubyLspTestCommandArgs? {
            if (arguments.isNullOrEmpty()) return null
            val path = extractPath(arguments.getOrNull(0)) ?: return null
            val name = stringFromAny(arguments.getOrNull(1))
            val command = stringFromAny(arguments.getOrNull(2))
            val range = extractRange(arguments.getOrNull(3))
            val start = range?.let { mapFromAny(it["start"]) }
            val startLine = start?.let { intValue(it, "line") } ?: range?.let { intValue(it, "start_line", "startLine") }
            val startColumn = start?.let { intValue(it, "character", "column") }
                ?: range?.let { intValue(it, "start_column", "startColumn") }

            return RubyLspTestCommandArgs(path, name, command, startLine, startColumn)
        }

        private fun extractPath(value: Any?): String? {
            val direct = stringFromAny(value)
            if (!direct.isNullOrBlank()) return direct

            val map = mapFromAny(value) ?: return null
            return stringFromAny(map["uri"]) ?: stringFromAny(map["path"])
        }

        private fun extractRange(value: Any?): Map<*, *>? {
            val direct = mapFromAny(value) ?: return null
            if (direct.containsKey("start") ||
                direct.containsKey("start_line") ||
                direct.containsKey("startLine")
            ) {
                return direct
            }
            return mapFromAny(direct["range"])
        }

        private fun mapFromAny(value: Any?): Map<*, *>? {
            return when (value) {
                is Map<*, *> -> value
                is JsonObject -> value.entrySet().associate { it.key to it.value }
                else -> null
            }
        }

        private fun stringFromAny(value: Any?): String? {
            return when (value) {
                is String -> value
                is JsonPrimitive -> if (value.isString) value.asString else value.toString()
                is JsonElement -> if (value.isJsonPrimitive) stringFromAny(value.asJsonPrimitive) else null
                else -> null
            }
        }

        private fun intValue(map: Map<*, *>, vararg keys: String): Int? {
            return keys.asSequence().firstNotNullOfOrNull { key -> intFromAny(map[key]) }
        }

        private fun intFromAny(value: Any?): Int? {
            return when (value) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                is JsonPrimitive -> when {
                    value.isNumber -> value.asInt
                    value.isString -> value.asString.toIntOrNull()
                    else -> null
                }
                is JsonElement -> if (value.isJsonPrimitive) intFromAny(value.asJsonPrimitive) else null
                else -> null
            }
        }
    }
}
