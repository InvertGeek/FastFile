@file:Suppress("MemberVisibilityCanBePrivate")

package com.donut.fastfile.util.objects

import android.view.WindowManager
import androidx.activity.ComponentActivity

open class MixActivity(private val id: String) : ComponentActivity() {

    init {
        referenceCache[id] = mutableSetOf()
    }

    var isActive = false
    var lastPause = System.currentTimeMillis()

    companion object {
        const val MAIN_ID = "main"
        val referenceCache = mutableMapOf<String, MutableSet<MixActivity>>()
        fun getContext(id: String) = referenceCache[id]?.firstOrNull { it.isActive }

        fun getMainContext() = getContext(MAIN_ID)

        fun firstActiveActivity(): MixActivity? {
            return referenceCache.values.flatten().maxByOrNull {
                if (it.isActive) Long.MAX_VALUE else it.lastPause
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        referenceCache[id]?.remove(this)
    }

    fun allowScreenshot() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun forbidScreenshot() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }


    override fun onPause() {
        isActive = false
        lastPause = System.currentTimeMillis()
        super.onPause()
    }


    override fun onResume() {
        isActive = true
        referenceCache[id]?.add(this)
        super.onResume()
    }


}
