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

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.decoder.AXrLottieResult;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author kienht
 * @since 01/02/2021
 */
public class AXrLottieTaskFactory {

    /**
     * Keep a map of cache keys to in-progress tasks and return them for new requests.
     * Without this, simultaneous requests to parse a composition will trigger multiple parallel
     * parse tasks prior to the cache getting populated.
     */
    private static final Map<String, AXrLottieTask<File>> taskCache = new HashMap<>();

    public static void clearCache() {
        taskCache.clear();
        AXrLottieTaskCache.getInstance().clear();
    }

    /**
     * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     * <p>
     * To skip the cache, add null as a third parameter.
     */
    public static AXrLottieTask<File> fromUrl(final String url, final boolean cache) {
        if (TextUtils.isEmpty(url)) return null;
        final String cacheKey = "url_" + url;
        return cache(cache, cacheKey, () -> {
            AXrLottieResult<File> result = AXrLottie.getNetworkFetcher().fetchSync(url, cache);
            File resultFile = result.getValue();
            if (resultFile != null) {
                AXrLottieTaskCache.getInstance().put(cacheKey, resultFile);
            }
            return result;
        });
    }

    /**
     * First, check to see if there are any in-progress tasks associated with the cache key and return it if there is.
     * If not, create a new task for the callable.
     * Then, add the new task to the task cache and set up listeners so it gets cleared when done.
     */
    private static AXrLottieTask<File> cache(final boolean cache, @Nullable final String cacheKey, Callable<AXrLottieResult<File>> callable) {
        if (cache && !TextUtils.isEmpty(cacheKey)) {
            final File cachedFile = AXrLottieTaskCache.getInstance().get(cacheKey);
            if (cachedFile != null) {
                return new AXrLottieTask<>(() -> new AXrLottieResult<>(cachedFile));
            }
        }

        if (cacheKey != null && taskCache.containsKey(cacheKey)) {
            return taskCache.get(cacheKey);
        }

        AXrLottieTask<File> task = new AXrLottieTask<>(callable);
        if (cacheKey != null) {
            task.addListener(result -> taskCache.remove(cacheKey));
            task.addFailureListener(result -> taskCache.remove(cacheKey));
            taskCache.put(cacheKey, task);
        }
        return task;
    }
}