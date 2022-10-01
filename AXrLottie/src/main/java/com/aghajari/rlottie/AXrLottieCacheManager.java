package com.aghajari.rlottie;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.aghajari.rlottie.extension.AXrFileExtension;
import com.aghajari.rlottie.extension.JsonFileExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Lottie cache manager
 */
public class AXrLottieCacheManager {

    private static final String TAG = AXrLottieCacheManager.class.getSimpleName();

    File networkCacheDir, localCacheDir;

    public AXrLottieCacheManager(File networkCacheDir, File localCacheDir) {
        this.networkCacheDir = networkCacheDir;
        this.localCacheDir = localCacheDir;
    }

    public void clear() {
        clear(getLocalCacheParent());
        clear(getNetworkCacheParent());
    }

    private void clear(File parentDir) {
        if (parentDir.exists()) {
            File[] files = parentDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    file.delete();
                }
            }
            parentDir.delete();
        }
    }

    /**
     * If the animation doesn't exist in the cache, null will be returned.
     * <p>
     * Once the animation is successfully parsed, {@link #loadTempFile(String, boolean)} must be
     * called to move the file from a temporary location to its permanent cache location so it can
     * be used in the future.
     */
    @Nullable
    @WorkerThread
    public File fetchURLFromCache(String url) {
        File jsonFile = getCachedFile(url, JsonFileExtension.JSON, true, false);
        if (jsonFile.exists()) {
            return jsonFile;
        } else {
            for (AXrFileExtension extension : AXrLottie.getSupportedFileExtensions().values()) {
                File file = getCachedFile(url, extension, true, false);
                if (file.exists()) {
                    file.delete();
                }
            }
            return null;
        }
    }

    public File fetchLocalFromCache(final String json, final String name) {
        File f = new File(getLocalCacheParent(), findCacheName(name, JsonFileExtension.JSON, false, false) + ".cache");
        if (f.exists()) return f;
        return writeLocalCache(json, f);
    }

    private File writeLocalCache(final String json, File file) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(json);
            outputStreamWriter.close();
            return file;
        } catch (IOException e) {
            if (file.exists()) file.delete();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Writes an InputStream from a network response to a temporary file. If the file successfully parses
     * to an composition, {@link #loadTempFile(String, boolean)}  should be called to move the file
     * to its final location for future cache hits.
     */
    public File writeTempCacheFile(String cache, InputStream stream, AXrFileExtension extension, boolean fromNetwork) throws IOException {
        return writeCacheFile(cache, stream, extension, fromNetwork, true);
    }

    public File writeCacheFile(String cache, InputStream stream, AXrFileExtension extension, boolean fromNetwork, boolean isTemp) throws IOException {
        String fileName = findCacheName(cache, extension, fromNetwork, isTemp);
        File file = new File(getParent(fromNetwork), fileName);
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
                if (file.exists()) file.delete();
                Log.e(TAG, "writeCacheFile: ", e);
            } finally {
                output.close();
            }
        } catch (Exception e) {
            if (file.exists()) file.delete();
            Log.e(TAG, "writeCacheFile: ", e);
        } finally {
            stream.close();
        }
        return file;
    }

    /**
     * If the file created by {@link #writeTempCacheFile(String, InputStream, AXrFileExtension, boolean)} was successfully parsed,
     * this should be called to remove the temporary part of its name which will allow it to be a cache hit in the future.
     */
    public File loadTempFile(String cache, boolean fromNetwork) {
        String fileName = findCacheName(cache, JsonFileExtension.JSON, fromNetwork, true);
        File file = new File(getParent(fromNetwork), fileName);
        if (!file.exists()) return null;
        String newFileName = file.getAbsolutePath().replace(".temp", "");
        File newFile = new File(newFileName);
        file.renameTo(newFile);
        return newFile;
    }

    /**
     * Returns the cache file for the given url if it exists.
     * Returns null if neither exist.
     */
    public File getCachedFile(String cache, AXrFileExtension extension, boolean fromNetwork, boolean isTemp) {
        return new File(getParent(fromNetwork), findCacheName(cache, extension, fromNetwork, isTemp));
    }

    private File getParent(boolean fromNetwork) {
        return fromNetwork ? getNetworkCacheParent() : getLocalCacheParent();
    }

    public File getNetworkCacheParent() {
        File file = networkCacheDir;
        if (file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public File getLocalCacheParent() {
        File file = localCacheDir;
        if (file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private String findCacheName(String url, AXrFileExtension extension, boolean fromNetwork, boolean isTemp) {
        return (fromNetwork ? "lottie_cache_" : "") + url.replaceAll("\\W+", "") + (isTemp ? extension.tempExtension() : extension.extension);
    }
}