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

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieDrawable;

class NetworkFetcher {
    private final Context context;
    private final String url;
    private AXrLottieNetworkFetcher fetcher;

    NetworkFetcher(Context context, String url, AXrLottieDrawable drawable, AXrLottieNetworkFetcher fetcher) {
        this.url = url;
        this.context = context;
        this.fetcher = fetcher;
        fetcher.attachToDrawable(drawable, url);
    }

    @WorkerThread
    void fetchSync() {
        File res = null;
        if (fetcher.isCacheEnabled()) res = fetchFromCache();
        if (res == null && !NetworkCache.checkLoading(url, fetcher)) {
            fetchFromNetwork();
            return;
        }
        if (res == null) return;
        load(res);
    }

    @WorkerThread
    private void load(final File res) {
        fetcher.load(res);
        NetworkCache.finishLoading(res, url, fetcher);
    }

    /**
     * Returns null if the animation doesn't exist in the cache.
     */
    @Nullable
    @WorkerThread
    private File fetchFromCache() {
        Pair<FileExtension, File> cacheResult = NetworkCache.fetch(context,fetcher.getDecodedUrl(url),fetcher.getCacheName(), fetcher.getSupportedExtensions());
        if (cacheResult == null) {
            return null;
        }
        File f = cacheResult.second;
        if (f == null || !f.exists()) return null;
        return f;
    }

    @WorkerThread
    private void fetchFromNetwork() {
        try {
            fetchFromNetworkInternal();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @WorkerThread
    private void fetchFromNetworkInternal() throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(AXrLottie.getNetworkTimeOut());
                    connection.setReadTimeout(AXrLottie.getNetworkTimeOut());

                    try {
                        connection.connect();
                        if (connection.getErrorStream() != null || connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            fetcher.error(connection.getErrorStream(), connection.getResponseCode());
                            return;
                        }

                        load(getResultFromConnection(connection));
                    } catch (Exception e) {
                        fetcher.error(e);
                    } finally {
                        connection.disconnect();
                    }
                } catch (MalformedURLException e1) {
                    fetcher.error(e1);
                } catch (IOException e) {
                    fetcher.error(e);
                }
            }
        });
        thread.start();
    }

    @WorkerThread
    private File getResultFromConnection(HttpURLConnection connection) throws IOException {
        return fetcher.getResultFromConnection(context, connection);
    }

}
