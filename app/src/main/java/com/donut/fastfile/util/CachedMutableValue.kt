package com.donut.fastfile.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.donut.fastfile.appScope
import com.donut.fastfile.kv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


fun <T> constructCachedMutableValue(
    value: T,
    setVal: (value: T) -> Unit,
    getVal: () -> T,
) =
    object : CachedMutableValue<T>(value) {
        override fun readCachedValue(): T {
            return getVal()
        }

        override fun writeCachedValue(value: T) {
            setVal(value)
        }
    }


fun cachedMutableOf(value: String, key: String) =
    constructCachedMutableValue(
        value,
        { kv.encode(key, it) },
        { kv.decodeString(key, value)!! })

fun cachedMutableOf(value: Boolean, key: String) =
    constructCachedMutableValue(value, { kv.encode(key, it) }, { kv.decodeBool(key, value) })

fun cachedMutableOf(value: Long, key: String) =
    constructCachedMutableValue(value, { kv.encode(key, it) }, { kv.decodeLong(key, value) })

fun cachedMutableOf(value: Set<String>, key: String) =
    constructCachedMutableValue(
        value,
        { kv.encode(key, it) },
        { kv.decodeStringSet(key, value)!! },
    )


inline fun <reified T, reified C : Iterable<T>> cachedMutableOf(value: C, key: String) =
    constructCachedMutableValue(
        value,
        {
            errorDialog("保存数据失败 key=${key}") {
                kv.encode(key, it.toJsonString())
            }
        },
        getter@{
            if (!kv.containsKey(key)) {
                return@getter value
            }
            errorDialog("读取数据失败 key=${key}") {
                kv.decodeString(key)?.parseJsonObject()
            } ?: value
        }
    )


abstract class CachedMutableValue<T>(
    @Volatile
    private var value: T,
) {
    @Volatile
    private var loaded = false
    private val lock = Any()
    private val mutex = Mutex()

    private var stateValue by mutableLongStateOf(0)

    abstract fun readCachedValue(): T

    abstract fun writeCachedValue(value: T)

    operator fun getValue(thisRef: Any?, property: Any?): T {
        synchronized(lock) {
            if (!loaded) {
                value = readCachedValue()
                loaded = true
            }
            stateValue
            return value
        }
    }


    operator fun setValue(thisRef: Any?, property: Any?, value: T) {
        synchronized(lock) {
            if (this.value == value) {
                return
            }
            stateValue++
            this.value = value
            appScope.launch(Dispatchers.IO) {
                mutex.withLock {
                    writeCachedValue(this@CachedMutableValue.value)
                }
            }
        }
    }
}