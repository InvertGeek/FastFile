package com.donut.fastfile.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import com.donut.fastfile.app
import com.donut.fastfile.appScope

import io.ktor.client.request.forms.FormBuilder
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.content.OutgoingContent
import io.ktor.http.defaultForFilePath
import io.ktor.http.quote

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.jvm.javaio.toOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.NetworkInterface
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

fun String.copyToClipboard(showToast: Boolean = true) {
    val clipboard = getClipBoard()
    val clip = ClipData.newPlainText("Copied Text", this)
    clipboard.setPrimaryClip(clip)
    if (showToast) showToast("复制成功")
}

fun getClipBoard(context: Context = app.applicationContext): ClipboardManager {
    return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}

fun readClipBoardText(): String {
    val clipboard = getClipBoard()
    val clip = clipboard.primaryClip
    if (clip != null && clip.itemCount > 0) {
        val text = clip.getItemAt(0).text
        return text?.toString() ?: ""
    }
    return ""
}

class StreamContent(private val stream: InputStream, val length: Long = 0) :
    OutgoingContent.WriteChannelContent() {
    override suspend fun writeTo(channel: ByteWriteChannel) {
        stream.copyTo(channel.toOutputStream())
    }

    override val contentLength: Long
        get() = length

}


fun formatFileSize(bytes: Long, mb: Boolean = false): String {
    if (bytes <= 0) return "0 B"
    if (mb && bytes > 1024 * 1024) {
        return String.format(
            Locale.US,
            "%.2f MB",
            bytes / 1024.0 / 1024.0
        )
    }
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    return String.format(
        Locale.US,
        "%.2f %s",
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units.getOrNull(digitGroups)
    )
}

fun getAppVersion(context: Context): Pair<String?, Long> {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
        Pair(versionName, versionCode)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        Pair("Unknown", -1L)
    }
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}


class CachedDelegate<T>(val getKeys: () -> Array<Any?>, private val initializer: () -> T) {
    private var cache: T? = null
    private var keys: Array<Any?> = arrayOf()

    operator fun getValue(thisRef: Any?, property: Any?): T {
        val newKeys = getKeys()
        if (cache == null || !keys.contentEquals(newKeys)) {
            keys = newKeys
            cache = initializer()
        }
        return cache!!
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: T) {
        cache = value
    }
}


tailrec fun String.hashToMD5String(round: Int = 1): String {
    val digest = hashMD5()
    if (round > 1) {
        return digest.toHex().hashToMD5String(round - 1)
    }
    return digest.toHex()
}

fun ByteArray.toHex(): String {
    val sb = StringBuilder()
    for (b in this) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}

fun String.hashMD5() = calculateHash("MD5")

fun String.hashSHA256() = calculateHash("SHA-256")

fun String.calculateHash(algorithm: String): ByteArray {
    val md = MessageDigest.getInstance(algorithm)
    md.update(this.toByteArray())
    return md.digest()
}

fun ByteArray.calculateHash(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    md.update(this)
    return md.digest().toHex()
}

fun ByteArray.hashSHA256() = calculateHash("SHA-256")

inline fun String.isUrl(block: (URL) -> Unit = {}): Boolean {
    val urlPattern =
        Regex("^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)\$")
    val result = urlPattern.matches(this)
    if (result) {
        ignoreError {
            block(URL(this))
        }
    }
    return result
}

fun getUrlHost(url: String): String? {
    url.isUrl {
        return it.host
    }
    return null
}

fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.encodeToBase64() = Base64.encode(this)

@OptIn(ExperimentalEncodingApi::class)
fun String.decodeBase64() = Base64.decode(this)

@OptIn(ExperimentalEncodingApi::class)
fun String.decodeBase64String() = Base64.decode(this).decodeToString()

fun String.encodeToBase64() = this.toByteArray().encodeToBase64()

fun <T> List<T>.at(index: Long): T {
    var fixedIndex = index % this.size
    if (fixedIndex < 0) {
        fixedIndex += this.size
    }
    return this[fixedIndex.toInt()]
}

fun <T> List<T>.at(index: Int): T {
    return this.at(index.toLong())
}

infix fun <T> List<T>.elementEquals(other: List<T>): Boolean {
    if (this.size != other.size) return false

    val tracker = BooleanArray(this.size)
    var counter = 0

    root@ for (value in this) {
        destination@ for ((i, o) in other.withIndex()) {
            if (tracker[i]) {
                continue@destination
            } else if (value?.equals(o) == true) {
                counter++
                tracker[i] = true
                continue@root
            }
        }
    }

    return counter == this.size
}


typealias UnitBlock = () -> Unit


fun debug(text: String?, tag: String = "test") {
    Log.d(tag, text ?: "null")
}

fun String.encodeURL(): String? {
    return URLEncoder.encode(this, "UTF-8")
}

fun String.getFileExtension(): String {
    val index = this.lastIndexOf('.')
    return if (index == -1) "" else this.substring(index + 1).lowercase()
}

fun String.parseFileMimeType() = ContentType.defaultForFilePath(this)

inline fun catchError(tag: String = "", block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        showError(e, tag)
    }
}

inline fun <T> ignoreError(block: () -> T): T? {
    try {
        return block()
    } catch (_: Exception) {

    }
    return null
}


fun getCurrentDate(reverseDays: Long = 0): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Date(System.currentTimeMillis() - (reverseDays * 86400 * 1000)))
}

fun getCurrentTime(): String {
    val currentTime = Date()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return formatter.format(currentTime)
}

fun genRandomString(
    length: Int = 32,
    chars: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
): String {
    return (1..length)
        .map { Random.nextInt(0, chars.size) }
        .map(chars::get)
        .joinToString("")
}

fun compressGzip(input: String): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    GZIPOutputStream(byteArrayOutputStream).use { gzip ->
        gzip.write(input.toByteArray())
    }
    return byteArrayOutputStream.toByteArray()
}

fun decompressGzip(compressed: ByteArray): String {
    val byteArrayInputStream = ByteArrayInputStream(compressed)
    GZIPInputStream(byteArrayInputStream).use { gzip ->
        return gzip.bufferedReader().use { it.readText() }
    }
}


fun isValidUri(uriString: String): Boolean {
    try {
        val uri = Uri.parse(uriString)
        return uri != null && uri.scheme != null
    } catch (e: Exception) {
        return false
    }
}

fun showError(e: Throwable, tag: String = "") {
    Log.e(
        "error",
        "${tag}发生错误: ${e.message} ${e.stackTraceToString()}"
    )
}

fun getIpAddressInLocalNetwork(): String {
    val networkInterfaces = NetworkInterface.getNetworkInterfaces().iterator().asSequence()
    val localAddresses = networkInterfaces.flatMap {
        it.inetAddresses.asSequence()
            .filter { inetAddress ->
                inetAddress.isSiteLocalAddress && inetAddress?.hostAddress?.contains(":")
                    .isFalse() &&
                        inetAddress.hostAddress != "127.0.0.1"
            }
            .map { inetAddress -> inetAddress.hostAddress }
    }
    return localAddresses.firstOrNull() ?: "127.0.0.1"
}

inline fun errorDialog(title: String, block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        if (e is CancellationException) {
            return
        }
        appScope.launch(Dispatchers.Main) {
            showErrorDialog(e, title)
        }
    }
}


@OptIn(InternalAPI::class)
fun FormBuilder.add(key: String, value: Any?, headers: Headers = Headers.Empty) {
    append(key.quote(), value ?: "", headers)
}

fun CoroutineScope.loopTask(
    delay: Long,
    initDelay: Long = 0,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend () -> Unit
) = launch(dispatcher) {
    delay(initDelay)
    while (true) {
        block()
        delay(delay)
    }
}


fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return formatter.format(date)
}

fun Uri.getFileName(): String {
    var fileName = ""
    app.contentResolver.query(this, null, null, null, null)?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        fileName = it.getString(nameIndex)
    }
    return fileName
}

fun Uri.getFileSize() =
    app.contentResolver.openAssetFileDescriptor(this, "r")?.use { it.length } ?: 0