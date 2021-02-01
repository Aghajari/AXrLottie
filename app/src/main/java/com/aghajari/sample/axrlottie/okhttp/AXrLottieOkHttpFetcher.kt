package com.aghajari.sample.axrlottie.okhttp

import com.aghajari.rlottie.network.AXrLottieFetchResult
import com.aghajari.rlottie.network.AXrLottieNetworkFetcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * @author kienht
 * @since 01/02/2021
 */
class AXrLottieOkHttpFetcher : AXrLottieNetworkFetcher {

    private val okHttpClient = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(
                    HttpLoggingInterceptor()
                            .apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            }
            )
            .retryOnConnectionFailure(true)
            .build()

    override fun fetchSync(url: String): AXrLottieFetchResult {
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        return AxrLottieOkHttpFetchResult(response)
    }
}