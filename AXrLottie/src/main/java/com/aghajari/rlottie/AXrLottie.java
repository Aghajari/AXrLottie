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

import androidx.annotation.NonNull;

import com.aghajari.rlottie.network.AXrLottieConfig;

import java.io.File;
import java.io.InputStream;

/**
 * @author Amir Hossein Aghajari
 * @version 1.0.3
 */
public class AXrLottie {

    static {
        System.loadLibrary("jlottie");
    }

    private AXrLottie() {
    }

    public static void init(@NonNull final AXrLottieConfig config) {
        AXrL.setContext(config.context);
        AXrL.setFetcher(config.networkFetcher);
        AXrL.setCacheProvider(config.cacheProvider);
        AXrL.setCacheEnabled(config.cacheEnabled);
        AXrL.setCacheSize(config.cacheSize);
        AXrL.setScreenRefreshRate(config.screenRefreshRate);
    }

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
        return AXrLottieDrawable.fromAssets(context, fileName)
                .setCacheName(name)
                .setSize(width, height)
                .setCacheEnabled(cache)
                .setFpsLimit(limitFps)
                .build();
    }

    public static AXrLottieDrawable createFromAssets(Context context, String fileName, String name, int width, int height, boolean startDecode) {
        return AXrLottieDrawable.fromAssets(context, fileName)
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
