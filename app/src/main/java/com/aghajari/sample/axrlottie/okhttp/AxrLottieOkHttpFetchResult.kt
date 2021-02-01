package com.aghajari.sample.axrlottie.okhttp

import android.util.Log
import com.aghajari.rlottie.network.AXrLottieFetchResult
import okhttp3.Response
import java.io.IOException
import java.io.InputStream

/**
 * @author kienht
 * @since 01/02/2021
 */
class AxrLottieOkHttpFetchResult(private val response: Response) : AXrLottieFetchResult {

    override fun close() {
        try {
            response.body?.close()
            response.close()
        } catch (e: Exception) {
            Log.e(TAG, "close: ", e)
        }
    }

    override fun isSuccessful(): Boolean = response.isSuccessful || response.code / 100 == 2

    override fun bodyByteStream(): InputStream = response.body?.byteStream() ?: throw IOException()

    override fun contentType(): String? = response.body?.contentType()?.type

    override fun error(): String? = response.body?.string()

    companion object {
        private val TAG: String = AxrLottieOkHttpFetchResult::class.java.simpleName
    }
}