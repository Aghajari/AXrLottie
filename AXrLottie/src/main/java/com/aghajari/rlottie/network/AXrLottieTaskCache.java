package com.aghajari.rlottie.network;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.collection.LruCache;

import java.io.File;

/**
 * @author kienht
 * @since 01/02/2021
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class AXrLottieTaskCache {

    private static final AXrLottieTaskCache INSTANCE = new AXrLottieTaskCache();

    public static AXrLottieTaskCache getInstance() {
        return INSTANCE;
    }

    private final LruCache<String, File> cache = new LruCache<>(20);

    @VisibleForTesting
    AXrLottieTaskCache() {
    }

    @Nullable
    public File get(@Nullable String cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        return cache.get(cacheKey);
    }

    public void put(@Nullable String cacheKey, File composition) {
        if (cacheKey == null) {
            return;
        }
        cache.put(cacheKey, composition);
    }

    public void clear() {
        cache.evictAll();
    }

    /**
     * Set the maximum number of compositions to keep cached in memory.
     * This must be {@literal >} 0.
     */
    public void resize(int size) {
        cache.resize(size);
    }
}