package com.donut.fastfile.util.file

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.donut.fastfile.MainActivity
import com.donut.fastfile.ui.component.common.MixDialogBuilder
import com.donut.fastfile.ui.theme.colorScheme
import com.donut.fastfile.util.AsyncEffect
import com.donut.fastfile.util.copyToClipboard
import com.donut.fastfile.util.errorDialog
import com.donut.fastfile.util.formatFileSize
import com.donut.fastfile.util.genRandomString
import com.donut.fastfile.util.getFileName
import com.donut.fastfile.util.getFileSize
import com.donut.fastfile.util.objects.ProgressContent
import com.donut.fastfile.util.showConfirmDialog
import com.donut.fastfile.util.showToast
import com.donut.fastfile.util.uploaders.DefaultUploader
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun fileFormHeaders(
    suffix: String = ".gif",
    mimeType: String = "image/gif",
): Headers {
    return Headers.build {
        append(HttpHeaders.ContentType, mimeType)
        append(
            HttpHeaders.ContentDisposition,
            "filename=\"${genRandomString(5)}${suffix}\""
        )
    }
}

fun showFileResult(dataLog: FileDataLog) {
    MixDialogBuilder("文件信息").apply {
        setContent {
            val context = LocalContext.current
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "名称: ${dataLog.name}")
                Text(text = "大小: ${formatFileSize(dataLog.size)}")
                Text(
                    text = dataLog.url,
                    color = colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        val intent =
                            Intent(
                                Intent.ACTION_VIEW,
                                dataLog.url.toUri()
                            )
                        context.startActivity(intent)
                    })
            }
        }
        setPositiveButton("复制") {
            dataLog.url.copyToClipboard()
        }
        show()
    }
}

fun uploadUri(uri: Uri) {
    MixDialogBuilder(
        "上传中",
        autoClose = false
    ).apply {
        setContent {
            val progressContent = remember {
                ProgressContent("上传中")
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                progressContent.LoadingContent()
            }
            AsyncEffect {
                val fileSize = uri.getFileSize()
                val fileName = uri.getFileName()
                errorDialog("上传失败") {
                    val fileUrl = DefaultUploader.doUpload(uri, progressContent)
                    withContext(Dispatchers.Main) {
                        val log = FileDataLog(
                            fileUrl,
                            fileName,
                            fileSize
                        )
                        addUploadLog(log)
                        showFileResult(log)
                    }
                    showToast("上传成功!")
                }
                closeDialog()
            }
        }
        setNegativeButton("取消") {
            showConfirmDialog("确定取消?") {
                showToast("上传已取消")
                closeDialog()
            }
        }
        show()
    }

}

@SuppressLint("Recycle")
fun selectAndUploadFile() {
    MainActivity.mixFileSelector.openSelect { uri ->
        uploadUri(uri)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoText(key: String, value: String) {
    FlowRow {
        Text(text = key, fontSize = 14.sp, color = Color(117, 115, 115, 255))
        Text(
            text = value,
            color = colorScheme.primary.copy(alpha = 0.8f),
            textDecoration = TextDecoration.Underline,
            fontSize = 14.sp,
        )
    }
}