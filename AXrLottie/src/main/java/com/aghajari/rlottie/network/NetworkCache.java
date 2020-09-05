/*
 * Copyright (C) 2020 - Amir Hossein Aghajari
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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to save and restore animations fetched from an URL to the app disk cache.
 */
public class NetworkCache {

    private static Map<String, List<AXrLottieNetworkFetcher>> loadingUrls = null;

    static boolean checkLoading(String url, AXrLottieNetworkFetcher fetcher) {
        if (loadingUrls == null) loadingUrls = new HashMap<>();
        if (loadingUrls.containsKey(url)) {
            List<AXrLottieNetworkFetcher> list = loadingUrls.get(url);
            list.add(fetcher);
            loadingUrls.put(url, list);
            return true;
        } else {
            List<AXrLottieNetworkFetcher> list = new ArrayList<>();
            list.add(fetcher);
            loadingUrls.put(url, list);
            return false;
        }
    }

    static void finishLoading(final File res, final String url,final AXrLottieNetworkFetcher fetcher) {
        if (loadingUrls == null || !loadingUrls.containsKey(url)) return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                    List<AXrLottieNetworkFetcher> list = loadingUrls.get(url);
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i)!=fetcher){
                            list.get(i).load(res);
                        }
                    }
                    loadingUrls.remove(url);
            }
        });
    }

    /**
     * If the animation doesn't exist in the cache, null will be returned.
     * <p>
     * Once the animation is successfully parsed, {@link #renameTempFile(Context, String, String, FileExtension)} must be
     * called to move the file from a temporary location to its permanent cache location so it can
     * be used in the future.
     */
    @Nullable
    @WorkerThread
    public static Pair<FileExtension, File> fetch(Context context, String url, String cacheName,FileExtension[] extensions) {
        File cachedFile = getCachedFile(context, url,cacheName,extensions);
        if (cachedFile == null) {
            return null;
        }

        FileExtension extension = new FileExtension(
                cachedFile.getAbsolutePath().substring(cachedFile.getAbsolutePath().lastIndexOf(".")));

        return new Pair<>(extension, cachedFile);
    }

    /**
     * Writes an InputStream from a network response to a temporary file. If the file successfully parses
     * to an composition, {@link #renameTempFile(Context, String, String, FileExtension)} should be called to move the file
     * to its final location for future cache hits.
     */
    public static File writeTempCacheFile(Context context, String url,String cacheName, InputStream stream, FileExtension extension) throws IOException {
        String fileName = filenameForUrl(url,cacheName, extension, true);
        File file = new File(context.getCacheDir(), fileName);
        try {
            OutputStream output = new FileOutputStream(file);
            //noinspection TryFinallyCanBeTryWithResources
            try {
                byte[] buffer = new byte[1024];
                int read;

                while ((read = stream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }

                output.flush();
            } finally {
                output.close();
            }
        } finally {
            stream.close();
        }
        return file;
    }

    /**
     * If the file created by {@link #writeTempCacheFile(Context, String, String, InputStream, FileExtension)} was successfully parsed,
     * this should be called to remove the temporary part of its name which will allow it to be a cache hit in the future.
     */
    public static File renameTempFile(Context context, String url, String cacheName, FileExtension extension) {
        String fileName = filenameForUrl(url, cacheName,extension, true);
        File file = new File(context.getCacheDir(), fileName);
        String newFileName = file.getAbsolutePath().replace(".temp", "");
        File newFile = new File(newFileName);
        file.renameTo(newFile);
        return newFile;
    }

    /**
     * Returns the cache file for the given url if it exists. Checks for both json and zip.
     * Returns null if neither exist.
     */
    @Nullable
    public static File getCachedFile(Context context,String url, String cacheName, FileExtension[] extensions) {
        for (FileExtension extension : extensions){
            File file = new File(context.getCacheDir(), filenameForUrl(url,cacheName, extension, false));
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static File getCachedFile(Context context, String fileName) {
        return new File(context.getCacheDir(), fileName);
    }

    public static String filenameForUrl(String url, String cacheName, FileExtension extension, boolean isTemp) {
        return cacheName + (isTemp ? extension.tempExtension() : extension.extension);
    }
}
