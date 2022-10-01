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


package com.aghajari.rlottie;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.rlottie.network.AXrLottieNetworkFetcher;
import com.aghajari.rlottie.network.AXrLottieTaskCache;
import com.aghajari.rlottie.network.AXrLottieTaskFactory;
import com.aghajari.rlottie.network.AXrNetworkFetcher;
import com.aghajari.rlottie.network.AXrSimpleNetworkFetcher;
import com.aghajari.rlottie.extension.AXrFileExtension;
import com.aghajari.rlottie.extension.JsonFileExtension;
import com.aghajari.rlottie.extension.ZipFileExtension;
import com.getkeepsafe.relinker.ReLinker;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amir Hossein Aghajari
 * @version 1.4.0
 */
public class AXrLottie {

    private AXrLottie() {
    }

    static Context applicationContext;

    static float screenRefreshRate = 60;

    @Nullable
    private static AXrNetworkFetcher networkFetcher;

    @Nullable
    private static AXrLottieCacheManager cacheManager;

    private static boolean networkCacheEnabled = true;

    private static final Map<String, AXrFileExtension> fileExtensions = new HashMap<>();

    @Nullable
    private static AXrLottieOptions defaultOptions = null;

    public static void init(Context context) {
        ReLinker.loadLibrary(context, "jlottie");
        AXrLottie.applicationContext = context.getApplicationContext();
        loadScreenRefreshRate(context);

        addFileExtension(ZipFileExtension.ZIP);
        addFileExtension(JsonFileExtension.JSON);
    }

    public static Map<String, AXrFileExtension> getSupportedFileExtensions() {
        return fileExtensions;
    }

    public static void addFileExtension(AXrFileExtension fileExtension) {
        fileExtensions.put(fileExtension.extension.toLowerCase(), fileExtension);
    }

    public static void removeFileExtension(AXrFileExtension fileExtension) {
        fileExtensions.remove(fileExtension.extension.toLowerCase());
    }

    public static void configureModelCacheSize(int cacheSize) {
        AXrLottieNative.configureModelCacheSize(cacheSize);
    }

    @Nullable
    public static AXrLottieOptions getDefaultOptions() {
        return defaultOptions;
    }

    public static void setDefaultOptions(@Nullable AXrLottieOptions defaultOptions) {
        AXrLottie.defaultOptions = defaultOptions;
    }

    public static void loadScreenRefreshRate(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            Display display = manager.getDefaultDisplay();
            if (display != null) {
                screenRefreshRate = display.getRefreshRate();
            }
        }
    }

    public static void setScreenRefreshRate(float screenRefreshRate) {
        AXrLottie.screenRefreshRate = screenRefreshRate;
    }

    public static float getScreenRefreshRate() {
        return screenRefreshRate;
    }

    /**
     * Lottie has a default network fetching stack built on {@link java.net.HttpURLConnection}.
     * However, if you would like to hook into your own network stack
     * for performance, caching, or analytics, you may replace the internal stack with your own.
     */
    public static void setNetworkFetcher(@Nullable AXrLottieNetworkFetcher networkFetcher) {
        AXrLottie.networkFetcher = new AXrNetworkFetcher(networkFetcher != null ? networkFetcher : new AXrSimpleNetworkFetcher());
    }

    /**
     * Provide your own network cache directory.
     * By default, animations will be saved in your application's cacheDir/lottie_network.
     */
    public static void setNetworkCacheDir(@NonNull File file) {
        if (!file.isDirectory())
            throw new IllegalArgumentException("cache file must be a directory");
        getLottieCacheManager().networkCacheDir = file;
    }

    /**
     * Provide your own network cache directory.
     * By default, animations will be saved in your application's cacheDir/lottie.
     */
    public static void setLocalCacheDir(@NonNull File file) {
        if (!file.isDirectory())
            throw new IllegalArgumentException("cache file must be a directory");
        getLottieCacheManager().localCacheDir = file;
    }

    public static void setNetworkCacheEnabled(boolean cacheEnabled) {
        networkCacheEnabled = cacheEnabled;
    }

    public static boolean isNetworkCacheEnabled() {
        return networkCacheEnabled;
    }

    /**
     * Set the maximum number of compositions to keep cached in memory.
     * This must be {@literal >} 0.
     */
    public static void setMaxNetworkCacheSize(int cacheSize) {
        AXrLottieTaskCache.getInstance().resize(cacheSize);
    }

    public static void clearCache() {
        AXrLottieTaskFactory.clearCache();
        getLottieCacheManager().clear();
    }

    @NonNull
    public static AXrNetworkFetcher getNetworkFetcher() {
        AXrNetworkFetcher local = networkFetcher;
        if (local == null) {
            synchronized (AXrNetworkFetcher.class) {
                local = networkFetcher;
                if (local == null) {
                    networkFetcher = local = new AXrNetworkFetcher(new AXrSimpleNetworkFetcher());
                }
            }
        }
        return local;
    }

    @NonNull
    public static AXrLottieCacheManager getLottieCacheManager() {
        AXrLottieCacheManager local = cacheManager;
        if (local == null) {
            synchronized (AXrLottieCacheManager.class) {
                local = cacheManager;
                if (local == null) {
                    cacheManager = local = new AXrLottieCacheManager(
                            new File(applicationContext.getCacheDir(), "lottie_network"),
                            new File(applicationContext.getCacheDir(), "lottie"));
                }
            }
        }
        return local;
    }


    public static class Loader {
        public static AXrLottieDrawable createFromPath(String path, int width, int height, boolean precache, boolean limitFps) {
            return AXrLottieDrawable.fromPath(path)
                    .setSize(width, height)
                    .setCacheEnabled(precache)
                    .setFpsLimit(limitFps)
                    .build();
        }

        public static AXrLottieDrawable createFromFile(File file, int width, int height, boolean precache, boolean limitFps) {
            return AXrLottieDrawable.fromFile(file)
                    .setSize(width, height)
                    .setCacheEnabled(precache)
                    .setFpsLimit(limitFps)
                    .build();
        }

        public static AXrLottieDrawable createFromURL(String url, int width, int height, boolean precache, boolean limitFps) {
            return AXrLottieDrawable.fromURL(url)
                    .setSize(width, height)
                    .setCacheEnabled(precache)
                    .setFpsLimit(limitFps)
                    .build();
        }

        public static AXrLottieDrawable createFromJson(String json, String name, int width, int height) {
            return AXrLottieDrawable.fromJson(json, name)
                    .setSize(width, height)
                    .setCacheEnabled(false)
                    .setFpsLimit(false)
                    .build();
        }

        public static AXrLottieDrawable createFromJson(String json, String name, int width, int height, boolean cache, boolean limitFps) {
            return AXrLottieDrawable.fromJson(json, name)
                    .setSize(width, height)
                    .setCacheEnabled(cache)
                    .setFpsLimit(limitFps)
                    .build();
        }

        public static AXrLottieDrawable createFromAssets(Context context, String fileName, String name, int width, int height) {
            return AXrLottieDrawable.fromAssets(context, fileName)
                    .setCacheName(name)
                    .setSize(width, height)
                    .setCacheEnabled(false)
                    .setFpsLimit(false)
                    .build();
        }

        public static AXrLottieDrawable createFromAssets(Context context, String fileName, String name, int width, int height, boolean cache, boolean limitFps) {
            return AXrLottieDrawable.fromAssets(context, fileName, name)
                    .setCacheName(name)
                    .setSize(width, height)
                    .setCacheEnabled(cache)
                    .setFpsLimit(limitFps)
                    .build();
        }

        public static AXrLottieDrawable createFromAssets(Context context, String fileName, String name, int width, int height, boolean startDecode) {
            return AXrLottieDrawable.fromAssets(context, fileName, name)
                    .setCacheName(name)
                    .setSize(width, height)
                    .setCacheEnabled(false)
                    .setFpsLimit(false)
                    .setAllowDecodeSingleFrame(startDecode)
                    .build();
        }

        public static AXrLottieDrawable createFromRes(Context context, int res, String name, int width, int height) {
            return AXrLottieDrawable.fromRes(context, res, name)
                    .setSize(width, height)
                    .setCacheEnabled(false)
                    .setFpsLimit(false)
                    .build();
        }

        public static AXrLottieDrawable createFromRes(Context context, int res, String name, int width, int height, boolean startDecode) {
            return AXrLottieDrawable.fromRes(context, res, name)
                    .setSize(width, height)
                    .setCacheEnabled(false)
                    .setFpsLimit(false)
                    .setAllowDecodeSingleFrame(startDecode)
                    .build();
        }

        public static AXrLottieDrawable createFromRes(Context context, int res, String name, int width, int height, boolean cache, boolean limitFps) {
            return AXrLottieDrawable.fromRes(context, res, name)
                    .setSize(width, height)
                    .setCacheEnabled(cache)
                    .setFpsLimit(limitFps)
                    .build();
        }

        public static AXrLottieDrawable createFromInputStream(InputStream inputStream, String name, int width, int height) {
            return AXrLottieDrawable.fromInputStream(inputStream, name)
                    .setSize(width, height)
                    .setCacheEnabled(false)
                    .setFpsLimit(false)
                    .build();
        }

        public static AXrLottieDrawable createFromInputStream(InputStream inputStream, String name, int width, int height, boolean startDecode) {
            return AXrLottieDrawable.fromInputStream(inputStream, name)
                    .setSize(width, height)
                    .setCacheEnabled(false)
                    .setFpsLimit(false)
                    .setAllowDecodeSingleFrame(startDecode)
                    .build();
        }

        public static AXrLottieDrawable createFromInputStream(InputStream inputStream, String name, int width, int height, boolean cache, boolean limitFps) {
            return AXrLottieDrawable.fromInputStream(inputStream, name)
                    .setSize(width, height)
                    .setCacheEnabled(cache)
                    .setFpsLimit(limitFps)
                    .build();
        }
    }

}
