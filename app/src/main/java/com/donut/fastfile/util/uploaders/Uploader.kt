package com.donut.fastfile.util.uploaders

import android.net.Uri
import androidx.compose.runtime.Composable
import com.donut.fastfile.util.objects.ProgressContent

abstract class Uploader {
    abstract val name: String
    abstract suspend fun doUpload(file: Uri, progress: ProgressContent): String

    @Composable
    open fun Settings() {

    }
}





