package com.aghajari.rlottie.network;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.IOException;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * Implement this interface to handle network fetching manually when animations are requested via url. By default, Lottie will use an
 * {@link java.net.HttpURLConnection} under the hood but this enables you to hook into your own network stack. By default, Lottie will also handle caching the
 * animations but if you want to provide your own cache directory, you may implement {@link AXrLottieNetworkCacheProvider}.
 */
public interface AXrLottieNetworkFetcher {
    @WorkerThread
    @NonNull
    AXrLottieFetchResult fetchSync(@NonNull String url) throws IOException;
}

