package com.aghajari.rlottie.network;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author kienht
 * @since 29/01/2021
 */
public class AXrNetworkCache {

    public static final String TAG = AXrNetworkCache.class.getSimpleName();

    @NonNull
    private final AXrLottieNetworkCacheProvider cacheProvider;

    private final AXrFileExtension[] supportedExtensions = new AXrFileExtension[]{AXrFileExtension.ZIP, AXrFileExtension.JSON};

    public AXrNetworkCache(@NonNull AXrLottieNetworkCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void clear() {
        File parentDir = parentCacheDir();
        if (parentDir.exists()) {
            File[] files = parentDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : parentDir.listFiles()) {
                    file.delete();
                }
            }
            parentDir.delete();
        }
    }

    /**
     * Returns null if the animation doesn't exist in the cache.
     */
    @Nullable
    @WorkerThread
    public File fetchFromCache(String url) {
        Pair<AXrFileExtension, File> cacheResult = fetch(url, supportedExtensions);
        if (cacheResult == null) {
            return null;
        }
        File f = cacheResult.second;
        if (f == null || !f.exists()) return null;
        return f;
    }

    /**
     * If the animation doesn't exist in the cache, null will be returned.
     * <p>
     * Once the animation is successfully parsed, {@link #renameTempFile(String, AXrFileExtension)} must be
     * called to move the file from a temporary location to its permanent cache location so it can
     * be used in the future.
     */
    @Nullable
    @WorkerThread
    public Pair<AXrFileExtension, File> fetch(String url, AXrFileExtension[] extensions) {
        File cachedFile = null;
        for (AXrFileExtension extension : extensions) {
            File file = new File(parentCacheDir(), filenameForUrl(url, extension, false));
            if (file.exists()) {
                cachedFile = file;
                break;
            }
        }
        if (cachedFile == null) {
            return null;
        }

        AXrFileExtension extension;
        if (cachedFile.getAbsolutePath().substring(cachedFile.getAbsolutePath().lastIndexOf(".")).endsWith(".zip")) {
            extension = AXrFileExtension.ZIP;
        } else {
            extension = AXrFileExtension.JSON;
        }

        return new Pair<>(extension, cachedFile);
    }

    /**
     * Writes an InputStream from a network response to a temporary file. If the file successfully parses
     * to an composition, {@link #renameTempFile(String, AXrFileExtension)} should be called to move the file
     * to its final location for future cache hits.
     */
    public File writeTempCacheFile(String url, InputStream stream, AXrFileExtension extension) throws IOException {
        String fileName = filenameForUrl(url, extension, true);
        File file = new File(parentCacheDir(), fileName);
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
            } catch (Exception e) {
                Log.e(TAG, "writeTempCacheFile: ", e);
            } finally {
                output.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "writeTempCacheFile: ", e);
        } finally {
            stream.close();
        }
        return file;
    }

    /**
     * If the file created by {@link #writeTempCacheFile(String, InputStream, AXrFileExtension)} was successfully parsed,
     * this should be called to remove the temporary part of its name which will allow it to be a cache hit in the future.
     */
    public File renameTempFile(String url, AXrFileExtension extension) {
        String fileName = filenameForUrl(url, extension, true);
        File file = new File(parentCacheDir(), fileName);
        String newFileName = file.getAbsolutePath().replace(".temp", "");
        File newFile = new File(newFileName);
        file.renameTo(newFile);
        return newFile;
    }

    /**
     * Returns the cache file for the given url if it exists.
     * Returns null if neither exist.
     */
    public File getCachedFile(String url, AXrFileExtension extension, boolean isTemp) {
        return new File(parentCacheDir(), filenameForUrl(url, extension, isTemp));
    }

    private File parentCacheDir() {
        File file = cacheProvider.getCacheDir();
        if (file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private String filenameForUrl(String url, AXrFileExtension extension, boolean isTemp) {
        return "lottie_cache_" + url.replaceAll("\\W+", "") + (isTemp ? extension.tempExtension() : extension.extension);
    }
}
