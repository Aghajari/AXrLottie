package com.aghajari.sample.axrlottie.initializer

import android.content.Context
import androidx.startup.Initializer
import com.aghajari.rlottie.AXrLottie
import com.aghajari.rlottie.network.AXrLottieConfig
import com.aghajari.sample.axrlottie.okhttp.AXrLottieOkHttpFetcher
import java.io.File

/**
 * @author kienht
 * @since 01/02/2021
 */
class LottieInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val file = File(context.cacheDir, "lottie_network_cache")
        if (!file.exists()) {
            file.mkdir()
        }

        AXrLottie.init(
                AXrLottieConfig.Builder(context)
                        .setCacheEnabled(true)
                        .setNetworkCacheDir(file)
                        .setNetworkFetcher(AXrLottieOkHttpFetcher())
                        .build()
        );
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}