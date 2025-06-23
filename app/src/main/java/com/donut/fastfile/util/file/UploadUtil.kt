package com.donut.fastfile.util.file

import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.userAgent
import io.ktor.serialization.gson.GsonConverter
import io.ktor.serialization.gson.gson

val uploadClient = HttpClient(OkHttp).config {
    install(ContentNegotiation) {
        gson()
        register(ContentType.Text.Html, GsonConverter(GsonBuilder().create()))
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 1000 * 36000
    }
    install(DefaultRequest) {
        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
    }
}