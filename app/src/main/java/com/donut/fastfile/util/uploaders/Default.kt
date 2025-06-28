package com.donut.fastfile.util.uploaders


import android.net.Uri
import com.donut.directlink.Directlink
import com.donut.fastfile.app
import com.donut.fastfile.util.file.uploadClient
import com.donut.fastfile.util.getFileName
import com.donut.fastfile.util.getFileSize
import com.donut.fastfile.util.objects.ProgressContent
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess


object DefaultUploader : Uploader() {
    override val name: String
        get() = "default"

    fun getServerAddr() = "http://127.0.0.1:${Directlink.getPort()}/"


    override suspend fun doUpload(file: Uri, progress: ProgressContent): String {

        val resolver = app.contentResolver
        val fileStream = resolver.openInputStream(file) ?: throw Exception("打开文件失败")
        val response = uploadClient.put {
            url(getServerAddr() + "api/upload")
            parameter("name", file.getFileName())
            header("content-length", file.getFileSize())
            setBody(fileStream)
            onUpload(progress.ktorListener)
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()){
            throw Exception("上传失败: ${text}")
        }
        return text
    }

}