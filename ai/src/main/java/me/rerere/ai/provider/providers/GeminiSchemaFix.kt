package me.rerere.ai.provider.providers

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Recursively walks a JSON schema and ensures every property with
 * "type": "array" also contains an "items" field.
 * Gemini API rejects array properties that lack "items".
 */
fun fixGeminiArrayItems(element: JsonElement): JsonElement {
    if (element !is JsonObject) return element

    val obj = element.jsonObject
    val type = obj["type"]?.jsonPrimitive?.content

    return buildJsonObject {
        for ((key, value) in obj) {
            when {
                // Recursively fix nested properties map
                key == "properties" && value is JsonObject -> {
                    put(key, buildJsonObject {
                        for ((propName, propValue) in value.jsonObject) {
                            put(propName, fixGeminiArrayItems(propValue))
                        }
                    })
                }
                // Recursively fix items if it already exists
                key == "items" && value is JsonObject -> {
                    put(key, fixGeminiArrayItems(value))
                }
                // Pass through everything else
                else -> put(key, value)
            }
        }
        // Inject missing "items" for array types
        if (type == "array" && !obj.containsKey("items")) {
            put("items", buildJsonObject { put("type", JsonPrimitive("string")) })
        }
    }
}
