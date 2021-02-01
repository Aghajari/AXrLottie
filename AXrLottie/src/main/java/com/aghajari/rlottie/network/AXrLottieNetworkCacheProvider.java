package com.aghajari.rlottie.network;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * Interface for providing the custom cache directory where animations downloaded via url are saved.
 */
public interface AXrLottieNetworkCacheProvider {

    /**
     * Called during cache operations
     *
     * @return cache directory
     */
    @NonNull
    File getCacheDir();
}