package com.donut.fastfile.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.ConcurrentHashMap

// 创建一个全局 Json 实例，带常用配置
val json = Json {
    prettyPrint = false
    ignoreUnknownKeys = true
    encodeDefaults = true
}

// 序列化扩展函数：任意 Serializable 类型
inline fun <reified T> T.toJsonString(): String {
    return json.encodeToString(this)
}

// 反序列化扩展函数：把字符串转成对象
inline fun <reified T> String.parseJsonObject(): T {
    return json.decodeFromString(this)
}


fun JsonObject.getString(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

fun JsonObject.getInt(key: String): Int? =
    this[key]?.jsonPrimitive?.intOrNull

fun JsonObject.getBoolean(key: String): Boolean? =
    this[key]?.jsonPrimitive?.booleanOrNull


class ConcurrentHashMapSerializer<K, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : KSerializer<ConcurrentHashMap<K, V>> {

    private val mapSerializer = MapSerializer(keySerializer, valueSerializer)

    override val descriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ConcurrentHashMap<K, V>) {
        mapSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ConcurrentHashMap<K, V> {
        val map = mapSerializer.deserialize(decoder)
        return ConcurrentHashMap(map)
    }
}