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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.decoder.AXrLottieResult;
import com.aghajari.rlottie.decoder.AXrStreamParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author kienht
 * @since 29/01/2021
 */
public class AXrNetworkFetcher {

    private static final String TAG = AXrNetworkFetcher.class.getSimpleName();

    @NonNull
    public final AXrLottieNetworkFetcher fetcher;

    public AXrNetworkFetcher(@NonNull AXrLottieNetworkFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @NonNull
    @WorkerThread
    public AXrLottieResult<File> fetchSync(@NonNull final String url, @NonNull final Boolean cache) {
        AXrLottieFetchResult fetchResult = null;
        File file = null;
        try {
            if (AXrLottie.isNetworkCacheEnabled() && cache) {
                file = AXrLottie.getLottieCacheManager().fetchURLFromCache(url);
            }
            if (file != null) {
                return new AXrLottieResult<>(file);
            } else {
                fetchResult = fetcher.fetchSync(url);

                if (fetchResult.isSuccessful()) {
                    InputStream inputStream = fetchResult.bodyByteStream();
                    String contentType = fetchResult.contentType();

                    return AXrStreamParser.parseStream(inputStream, contentType, url, true);
                } else {
                    return new AXrLottieResult<>(new IllegalArgumentException(fetchResult.error()));
                }
            }
        } catch (Exception e) {
            return new AXrLottieResult<>(e);
        } finally {
            if (fetchResult != null) {
                try {
                    fetchResult.close();
                } catch (IOException e) {
                    Log.e(TAG, "LottieFetchResult close failed ", e);
                }
            }
        }
    }

    @WorkerThread
    protected AXrLottieResult<File> parseStream(InputStream inputStream, String contentType, String url) {
        try {
            if (contentType == null) {
                // Assume JSON for best effort parsing. If it fails, it will just deliver the parse exception
                // in the result which is more useful than failing here.
                contentType = "application/json";
            }

            boolean parsed = false;
            for (AXrFileExtension fileExtension : AXrLottie.getSupportedFileExtensions().values()) {
                if (fileExtension.canParseContent(contentType)) {
                    if (fileExtension.willReadStream()) {
                        parsed = fileExtension.toFile(url, inputStream, true) != null;
                    } else {
                        File input = AXrLottie.getLottieCacheManager().writeTempCacheFile(url, inputStream, fileExtension, true);
                        parsed = fileExtension.toFile(url, input, true) != null;
                        if (!parsed && input != null && input.exists())
                            input.delete();
                    }
                }
                if (parsed) break;
            }
            if (!parsed) {
                AXrLottie.getLottieCacheManager().writeTempCacheFile(url, inputStream, JsonFileExtension.JSON, true);
            }

            File file = AXrLottie.getLottieCacheManager().loadTempFile(url, true);
            return new AXrLottieResult<>(file);
        } catch (Exception e) {
            return new AXrLottieResult<>(e);
        }
    }
}
