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

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieDrawable;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

public abstract class AXrLottieNetworkFetcher {

    protected AXrLottieDrawable drawable;
    protected String url;

    public void attachToDrawable(AXrLottieDrawable drawable, String url) {
        this.drawable = drawable;
        this.url = url;
    }

    public FileExtension[] getSupportedExtensions() {
        return new FileExtension[]{FileExtension.ZIP(), FileExtension.JSON()};
    }

    public String getDecodedUrl(String url) {
        return url;
    }

    public abstract void load(File file);

    public abstract File getResultFromConnection(Context context, HttpURLConnection connection);

    public void error(Exception e) {
        e.printStackTrace();
    }

    public void error(InputStream errorInputStream, int responseCode) {
    }

    public String getCacheName() {
        return drawable.getCacheName();
    }

    public boolean isCacheEnabled() {
        return AXrLottie.isNetworkCacheEnabled();
    }

    public AXrLottieDrawable getDrawable() {
        return drawable;
    }

    public String getUrl() {
        return url;
    }

    public static void load(Context context, String url, AXrLottieDrawable drawable, AXrLottieNetworkFetcher fetcher) {
        new NetworkFetcher(context, url, drawable, fetcher).fetchSync();
    }
}
