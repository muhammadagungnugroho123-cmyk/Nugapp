package com.example.network

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var responseOK = false
        var tryCount = 0

        while (!responseOK && tryCount < maxRetries) {
            try {
                response = chain.proceed(request)
                responseOK = response.isSuccessful
            } catch (e: Exception) {
                if (tryCount >= maxRetries - 1) {
                    throw e
                }
            } finally {
                if (!responseOK && response != null && tryCount < maxRetries - 1) {
                    response.close()
                }
            }
            tryCount++
            if (!responseOK && tryCount < maxRetries) {
                try {
                    Thread.sleep(1000L * tryCount) // Exponential backoff basic
                } catch (e: InterruptedException) {
                    // ignore
                }
            }
        }
        return response ?: chain.proceed(request)
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(RetryInterceptor())
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}
