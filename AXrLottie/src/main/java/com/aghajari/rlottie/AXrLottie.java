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

import com.aghajari.rlottie.network.AXrLottieNetworkFetcher;

import java.io.File;
import java.io.InputStream;


/**
 *
 * @author Amir Hossein Aghajari
 * @version 1.0.3
 *
 */
public class AXrLottie {
    static {
        System.loadLibrary("jlottie");
    }

    private AXrLottie(){}

    static Context context;
    static float screenRefreshRate = 60;
    private static boolean urlCacheEnabled = true;
    private static int timeOut = 10000;

    public static void init(Context context){
        AXrLottie.context = context.getApplicationContext();
        loadScreenRefreshRate(context);
    }

    public static void configureModelCacheSize(int cacheSize){
        AXrLottieNative.configureModelCacheSize(cacheSize);
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

    public static void setNetworkCacheEnabled(boolean urlCacheEnabled) {
        AXrLottie.urlCacheEnabled = urlCacheEnabled;
    }

    public static boolean isNetworkCacheEnabled() {
        return urlCacheEnabled;
    }

    public static void setNetworkTimeOut(int timeOut) {
        AXrLottie.timeOut = timeOut;
    }

    public static int getNetworkTimeOut() {
        return timeOut;
    }

    public static AXrLottieDrawable createFromPath(String path, int width, int height, boolean precache, boolean limitFps){
        return AXrLottieDrawable.fromPath(path)
                .setSize(width,height)
                .setCacheEnabled(precache)
                .setFpsLimit(limitFps)
                .build();
    }

    public static AXrLottieDrawable createFromFile(File file,int width,int height,boolean precache, boolean limitFps){
         return AXrLottieDrawable.fromFile(file)
                .setSize(width,height)
                .setCacheEnabled(precache)
                .setFpsLimit(limitFps)
                .build();
    }

    public static AXrLottieDrawable createFromURL(String url,int width,int height,boolean precache, boolean limitFps){
        return AXrLottieDrawable.fromURL(url)
                .setSize(width,height)
                .setCacheEnabled(precache)
                .setFpsLimit(limitFps)
                .build();
    }

    public static AXrLottieDrawable createFromURL(String url, AXrLottieNetworkFetcher fetcher, int width, int height, boolean precache, boolean limitFps){
        return AXrLottieDrawable.fromURL(url,fetcher)
                .setSize(width,height)
                .setCacheEnabled(precache)
                .setFpsLimit(limitFps)
                .build();
    }

    public static AXrLottieDrawable createFromJson(String json, String name, int width, int height) {
        return AXrLottieDrawable.fromJson(json,name)
                .setSize(width,height)
                .setCacheEnabled(false)
                .setFpsLimit(false)
                .build();
    }

    public static AXrLottieDrawable createFromJson(String json, String name, int width, int height,boolean cache,boolean limitFps) {
        return AXrLottieDrawable.fromJson(json,name)
                .setSize(width,height)
                .setCacheEnabled(cache)
                .setFpsLimit(limitFps)
                .build();
    }

    public static AXrLottieDrawable createFromAssets(Context context,String fileName, String name, int width, int height) {
        return AXrLottieDrawable.fromAssets(context,fileName)
                .setCacheName(name)
                .setSize(width,height)
                .setCacheEnabled(false)
                .setFpsLimit(false)
                .build();
    }

    public static AXrLottieDrawable createFromAssets(Context context,String fileName, String name, int width, int height,boolean cache,boolean limitFps) {
        return AXrLottieDrawable.fromAssets(context,fileName)
                .setCacheName(name)
                .setSize(width,height)
                .setCacheEnabled(cache)
                .setFpsLimit(limitFps)
                .build();
    }

    public static AXrLottieDrawable createFromAssets(Context context,String fileName, String name, int width, int height,boolean startDecode) {
        return AXrLottieDrawable.fromAssets(context,fileName)
                .setCacheName(name)
                .setSize(width,height)
                .setCacheEnabled(false)
                .setFpsLimit(false)
                .setAllowDecodeSingleFrame(startDecode)
                .build();
    }

    public static AXrLottieDrawable createFromRes(Context context,int res, String name, int width, int height) {
        return AXrLottieDrawable.fromRes(context,res,name)
                .setSize(width,height)
                .setCacheEnabled(false)
                .setFpsLimit(false)
                .build();
    }

    public static AXrLottieDrawable createFromRes(Context context,int res, String name, int width, int height,boolean startDecode) {
        return AXrLottieDrawable.fromRes(context,res,name)
                .setSize(width,height)
                .setCacheEnabled(false)
                .setFpsLimit(false)
                .setAllowDecodeSingleFrame(startDecode)
                .build();
    }

    public static AXrLottieDrawable createFromRes(Context context,int res, String name, int width, int height,boolean cache,boolean limitFps) {
        return AXrLottieDrawable.fromRes(context,res,name)
                .setSize(width,height)
                .setCacheEnabled(cache)
                .setFpsLimit(limitFps)
                .build();
    }

    public static AXrLottieDrawable createFromInputStream(InputStream inputStream, String name, int width, int height) {
        return AXrLottieDrawable.fromInputStream(inputStream,name)
                .setSize(width,height)
                .setCacheEnabled(false)
                .setFpsLimit(false)
                .build();
    }

    public static AXrLottieDrawable createFromInputStream(InputStream inputStream, String name, int width, int height,boolean startDecode) {
        return AXrLottieDrawable.fromInputStream(inputStream,name)
                .setSize(width,height)
                .setCacheEnabled(false)
                .setFpsLimit(false)
                .setAllowDecodeSingleFrame(startDecode)
                .build();
    }

    public static AXrLottieDrawable createFromInputStream(InputStream inputStream, String name, int width, int height,boolean cache,boolean limitFps) {
        return AXrLottieDrawable.fromInputStream(inputStream,name)
                .setSize(width,height)
                .setCacheEnabled(cache)
                .setFpsLimit(limitFps)
                .build();
    }

}
