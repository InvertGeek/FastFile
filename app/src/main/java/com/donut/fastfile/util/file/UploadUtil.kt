package com.donut.fastfile.util.file

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.userAgent

val uploadClient = HttpClient(OkHttp).config {
    install(HttpTimeout) {
        requestTimeoutMillis = 1000 * 36000
    }
    install(DefaultRequest) {
        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
    }
}