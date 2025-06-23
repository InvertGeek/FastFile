package com.donut.fastfile.util.file

import androidx.compose.material3.Text
import com.donut.fastfile.ui.component.common.MixDialogBuilder
import com.donut.fastfile.util.cachedMutableOf
import com.donut.fastfile.util.showToast


data class FileDataLog(
    val url: String,
    val name: String,
    val size: Long,
    val time: Long = System.currentTimeMillis(),
)

var uploadLogs by cachedMutableOf(listOf<FileDataLog>(), "upload_file_logs")


fun addUploadLog(uploadLog: FileDataLog) {
    if (uploadLogs.size > 1000) {
        uploadLogs = uploadLogs.drop(1)
    }
    uploadLogs = uploadLogs + uploadLog
}


fun deleteUploadLog(uploadLog: FileDataLog, callback: () -> Unit = {}) {
    MixDialogBuilder("确定删除?").apply {
        setContent {
            Text(text = "确定从上传记录中删除?")
        }
        setPositiveButton("确定") {
            uploadLogs = uploadLogs.filter { it != uploadLog }
            closeDialog()
            callback()
            showToast("删除成功")
        }
        setDefaultNegative()
        show()
    }
}