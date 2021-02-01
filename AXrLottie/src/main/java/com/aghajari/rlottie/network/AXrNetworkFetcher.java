package com.aghajari.rlottie.network;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.aghajari.rlottie.AXrL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * @author kienht
 * @since 29/01/2021
 */
public class AXrNetworkFetcher {

    public static final String TAG = AXrNetworkFetcher.class.getSimpleName();

    @NonNull
    private final AXrNetworkCache networkCache;

    @NonNull
    private final AXrLottieNetworkFetcher fetcher;

    public AXrNetworkFetcher(@NonNull AXrNetworkCache networkCache, @NonNull AXrLottieNetworkFetcher fetcher) {
        this.networkCache = networkCache;
        this.fetcher = fetcher;
    }

    @NonNull
    @WorkerThread
    public AXrLottieResult<File> fetchSync(@NonNull final String url, @NonNull final Boolean cache) {
        AXrLottieFetchResult fetchResult = null;
        File file = null;
        try {
            if (AXrL.isCacheEnabled() && cache) {
                file = networkCache.fetchFromCache(url);
            }
            if (file != null) {
                return new AXrLottieResult<>(file);
            } else {
                fetchResult = fetcher.fetchSync(url);
                if (fetchResult.isSuccessful()) {
                    InputStream inputStream = fetchResult.bodyByteStream();
                    String contentType = fetchResult.contentType();

                    if (contentType == null) {
                        // Assume JSON for best effort parsing. If it fails, it will just deliver the parse exception
                        // in the result which is more useful than failing here.
                        contentType = "application/json";
                    }
                    if (ZipCompositionFactory.isZipContent(contentType)) {
                        file = networkCache.writeTempCacheFile(url, inputStream, AXrFileExtension.ZIP);
                        file = fromZipStream(
                                file,
                                networkCache.getCachedFile(url, AXrFileExtension.JSON, true),
                                new ZipInputStream(new FileInputStream(file))
                        );
                        Log.d(TAG, file.getAbsolutePath());
                    } else {
                        file = networkCache.writeTempCacheFile(url, inputStream, AXrFileExtension.JSON);
                    }
                    file = networkCache.renameTempFile(url, AXrFileExtension.JSON);
                    return new AXrLottieResult<>(file);
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

    private File fromZipStream(File file, File output, ZipInputStream stream) {
        return ZipCompositionFactory.fromZipStreamSyncInternal(file, output, stream);
    }
}
