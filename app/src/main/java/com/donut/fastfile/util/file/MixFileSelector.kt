package com.donut.fastfile.util.file

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.donut.fastfile.util.objects.MixActivity
import kotlinx.coroutines.suspendCancellableCoroutine

class MixFileSelector(activity: MixActivity) {
    private var fileSelector: ActivityResultLauncher<Array<String>>
    private var callback: (uri: Uri) -> Unit = { }

    init {
        fileSelector = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                callback(it)
            }
        }
    }

    fun unregister() {
        fileSelector.unregister()
    }

    suspend fun openSelect() = suspendCancellableCoroutine<Uri> { cont ->
        this.callback = {
            cont.resumeWith(Result.success(it))
        }
        fileSelector.launch(arrayOf("*/*"))
    }


    fun openSelect(
        array: Array<String> = arrayOf("*/*"),
        callback: (uri: Uri) -> Unit,
    ) {
        this.callback = callback
        fileSelector.launch(array)
    }
}