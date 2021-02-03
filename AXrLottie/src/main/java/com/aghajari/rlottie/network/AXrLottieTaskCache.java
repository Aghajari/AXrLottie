/*
 * Copyright (C) 2021 - Amir Hossein Aghajari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


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
public class AXrLottieTaskCache {

    private static final AXrLottieTaskCache INSTANCE = new AXrLottieTaskCache();

    private static final int DEFAULT_CACHE_SIZE = 20;

    public static AXrLottieTaskCache getInstance() {
        return INSTANCE;
    }

    private final LruCache<String, File> cache = new LruCache<>(DEFAULT_CACHE_SIZE);

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