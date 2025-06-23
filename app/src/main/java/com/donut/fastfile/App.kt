package com.donut.fastfile


import android.app.Application
import android.os.Looper
import com.donut.directlink.Directlink
import com.donut.fastfile.util.loopTask
import com.donut.fastfile.util.objects.MixActivity
import com.donut.fastfile.util.showError
import com.donut.fastfile.util.showErrorDialog
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


val appScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

lateinit var kv: MMKV

private lateinit var innerApp: Application


val currentActivity: MixActivity
    get() {
        return MixActivity.firstActiveActivity()!!
    }

val app: Application
    get() = innerApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            showError(e)
            if (Looper.myLooper() == null) {
                return@setDefaultUncaughtExceptionHandler
            }
            showErrorDialog(e)
        }
        innerApp = this
        MMKV.initialize(this)
        kv = MMKV.defaultMMKV()
        appScope.launch(Dispatchers.IO) {
            Directlink.startServer()
        }
        appScope.loopTask(1000 * 60 * 10) {
            kv.clearMemoryCache()
            kv.trim()
        }
    }

}

