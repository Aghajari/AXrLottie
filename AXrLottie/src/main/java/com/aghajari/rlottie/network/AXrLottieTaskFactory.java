package com.aghajari.rlottie.network;

import androidx.annotation.Nullable;

import com.aghajari.rlottie.AXrL;

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

    /**
     * Set the maximum number of compositions to keep cached in memory.
     * This must be {@literal >} 0.
     */
    public static void setMaxCacheSize(int size) {
        AXrLottieTaskCache.getInstance().resize(size);
    }

    public static void clearCache() {
        taskCache.clear();
        AXrLottieTaskCache.getInstance().clear();
        AXrL.networkCache().clear();
    }

    /**
     * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     * <p>
     * To skip the cache, add null as a third parameter.
     */
    public static AXrLottieTask<File> fromUrl(final String url, final boolean cache) {
        final String cacheKey = "url_" + url;
        return cache(cacheKey, new Callable<AXrLottieResult<File>>() {
            @Override
            public AXrLottieResult<File> call() {
                AXrLottieResult<File> result = AXrL.networkFetcher().fetchSync(url, cache);
                if (cacheKey != null && result.getValue() != null) {
                    AXrLottieTaskCache.getInstance().put(cacheKey, result.getValue());
                }
                return result;
            }
        });
    }

    /**
     * First, check to see if there are any in-progress tasks associated with the cache key and return it if there is.
     * If not, create a new task for the callable.
     * Then, add the new task to the task cache and set up listeners so it gets cleared when done.
     */
    private static AXrLottieTask<File> cache(
            @Nullable final String cacheKey, Callable<AXrLottieResult<File>> callable) {
        final File cachedFile = cacheKey == null ? null : AXrLottieTaskCache.getInstance().get(cacheKey);
        if (cachedFile != null) {
            return new AXrLottieTask<>(new Callable<AXrLottieResult<File>>() {
                @Override
                public AXrLottieResult<File> call() {
                    return new AXrLottieResult<>(cachedFile);
                }
            });
        }
        if (cacheKey != null && taskCache.containsKey(cacheKey)) {
            return taskCache.get(cacheKey);
        }

        AXrLottieTask<File> task = new AXrLottieTask<>(callable);
        if (cacheKey != null) {
            task.addListener(new AXrLottieListener<File>() {
                @Override
                public void onResult(File result) {
                    taskCache.remove(cacheKey);
                }
            });
            task.addFailureListener(new AXrLottieListener<Throwable>() {
                @Override
                public void onResult(Throwable result) {
                    taskCache.remove(cacheKey);
                }
            });
            taskCache.put(cacheKey, task);
        }
        return task;
    }
}
