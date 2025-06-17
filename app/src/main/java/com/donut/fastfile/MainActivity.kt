package com.donut.fastfile

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.donut.fastfile.ui.component.MainContent
import com.donut.fastfile.ui.theme.MainTheme
import com.donut.fastfile.util.file.MixFileSelector
import com.donut.fastfile.util.file.uploadUri
import com.donut.fastfile.util.objects.MixActivity
import com.donut.fastfile.util.objects.NetworkChangeReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var serverAddress by mutableStateOf("http://127.0.0.1:8080")


class MainActivity : MixActivity(MAIN_ID) {

    companion object {
        lateinit var mixFileSelector: MixFileSelector
    }

    override fun onDestroy() {
        super.onDestroy()
        mixFileSelector.unregister()
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        mixFileSelector = MixFileSelector(this)
        super.onCreate(savedInstanceState)
        registerReceiver(
            NetworkChangeReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        appScope.launch(Dispatchers.Main) {
            handleIntent()
        }
        enableEdgeToEdge()
        setContent {
            MainTheme {
                MainContent()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent()
        super.onNewIntent(intent)
    }

    private fun handleIntent() {
        val action = intent.action
        intent.type ?: return
        when (action) {
            Intent.ACTION_SEND -> {
                // 处理单文件分享
                val fileUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (fileUri != null) {
                    uploadUri(fileUri)
                }
            }

        }
    }
}






