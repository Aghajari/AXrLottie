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

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieDrawable;

import java.io.File;
import java.io.InputStream;

public abstract class AXrLottieNetworkFetcher {

    protected AXrLottieDrawable drawable;
    protected String cacheName;
    protected String url;

    public void attachToDrawable(Context context,AXrLottieDrawable drawable, String url,boolean load) {
        this.drawable = drawable;
        this.cacheName = this.drawable.getCacheName();
        this.url = url;

        if (load) fetchSync(context);
    }

    public FileExtension[] getSupportedExtensions() {
        return new FileExtension[]{
                FileExtension.ZIP(),
                FileExtension.JSON()
        };
    }

    public String getURL() {
        return url;
    }

    public String getCacheName() {
        return cacheName;
    }

    public boolean isCacheEnabled() {
        return AXrLottie.isNetworkCacheEnabled();
    }

    public AXrLottieDrawable getDrawable() {
        return drawable;
    }

    @WorkerThread
    public void fetchSync(Context context) {
        File res = null;
        if (isCacheEnabled()) res = fetchFromCache(context);

        if (res == null && !NetworkCache.checkLoading(url, this)) {
            fetchFromNetwork(context);
            return;
        }
        if (res == null) return;
        onLoad(res);
    }

    /**
     * Returns null if the animation doesn't exist in the cache.
     */
    @Nullable
    @WorkerThread
    protected File fetchFromCache(Context context) {
        Pair<FileExtension, File> cacheResult = NetworkCache.fetch(context, getCacheName(), getSupportedExtensions());
        if (cacheResult == null) {
            return null;
        }
        File f = cacheResult.second;
        if (f == null || !f.exists()) return null;
        return f;
    }

    @WorkerThread
    protected abstract void fetchFromNetwork(Context context);

    @WorkerThread
    protected void parseStream(Context context, InputStream inputStream, String contentType) {
        try {
            File file;
            if (contentType == null) {
                // Assume JSON for best effort parsing. If it fails, it will just deliver the parse exception
                // in the result which is more useful than failing here.
                contentType = "application/json";
            }

            FileExtension[] fileExtensions = getSupportedExtensions();
            boolean parsed = false;
            for (FileExtension fileExtension : fileExtensions) {
                if (fileExtension.canParseContent(contentType)){
                    parsed = fileExtension.saveAsTempFile(context, getCacheName(),inputStream) != null;
                }
                if (parsed) break;
            }
            if (!parsed)
                FileExtension.JSON().saveAsTempFile(context, getCacheName(),inputStream);

            file = NetworkCache.loadTempFile(context, getCacheName());

            onLoad(file);
        } catch (Exception e) {
            onError(new ParserException(e));
        }
    }

    @WorkerThread
    public void onLoad(File file) {
        if (file != null && file.exists()) {
            drawable.initFromNetwork(file,this);
        }
        NetworkCache.finishLoading(file, getURL(), this);
    }

    public void onError(Exception e){
        e.printStackTrace();
    }
}
