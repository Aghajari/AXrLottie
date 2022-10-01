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


package com.aghajari.rlottie.decoder;

import android.content.Context;

import androidx.annotation.Nullable;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.extension.AXrFileExtension;
import com.aghajari.rlottie.extension.JsonFileExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class to read local files
 */
public class AXrFileReader {

    public static File fromFile(File input) {
        String cache = "lottie_cache_" + input.getName();
        File file = AXrLottie.getLottieCacheManager().getCachedFile(cache, JsonFileExtension.JSON, false, true);
        if (file != null && file.exists())
            return file;

        for (AXrFileExtension extension : AXrLottie.getSupportedFileExtensions().values()) {
            if (extension.canParseFile(input)) {
                try {
                    File f = extension.toFile(cache, input, false);
                    if (f != null) return f;
                } catch (IOException ignore) {
                }
            }
        }
        return input;
    }

    public static File fromFile(File input, boolean fromNetwork, String name) {
        String cache = "lottie_cache_" + name;
        File file = AXrLottie.getLottieCacheManager().getCachedFile(cache, JsonFileExtension.JSON, fromNetwork, true);
        if (file != null && file.exists())
            return file;

        for (AXrFileExtension extension : AXrLottie.getSupportedFileExtensions().values()) {
            if (extension.canParseFile(name)) {
                try {
                    File f = extension.toFile(cache, input, fromNetwork);
                    if (f != null) return f;
                } catch (IOException ignore) {
                }
            }
        }
        return input;
    }

    public static String fromRes(Context context, int rawRes) {
        String cache = "lottie_cache_" + context.getResources().getResourceName(rawRes);
        return read(cache, context, null, rawRes);
    }

    public static String fromAssets(Context context, String fileName) {
        String cache = "lottie_cache_" + fileName;
        return read(cache, context, fileName, 0);
    }

    public static String fromInputStream(InputStream stream) {
        return readStream(stream);
    }

    private static String read(String cache, Context context, @Nullable String asset, int rawRes) {
        File file = AXrLottie.getLottieCacheManager().getCachedFile(cache, JsonFileExtension.JSON, false, true);
        if (file != null && file.exists()) {
            try {
                return readStream(new FileInputStream(file));
            } catch (FileNotFoundException ignore) {
            }
        }

        try {
            File input = null;
            for (AXrFileExtension extension : AXrLottie.getSupportedFileExtensions().values()) {
                if (extension.canParseFile(cache)) {
                    if (input == null) {
                        input = AXrLottie.getLottieCacheManager().writeTempCacheFile(cache, readRes(context, asset, rawRes), extension, false);
                    }
                    try {
                        File f = extension.toFile(cache, input, false);
                        if (f != null) return readStream(new FileInputStream(f));
                    } catch (IOException ignore) {
                    }
                    if (input != null && input.exists()) input.delete();
                }
            }
        } catch (Exception ignore) {
        }
        return readResAsString(context, asset, rawRes);
    }

    private static final ThreadLocal<byte[]> readBufferLocal = new ThreadLocal<>();
    private static final ThreadLocal<byte[]> bufferLocal = new ThreadLocal<>();

    private static String readResAsString(Context context, String asset, int rawRes) {
        return readStream(readRes(context, asset, rawRes));
    }

    private static InputStream readRes(Context context, @Nullable String asset, int rawRes) {
        InputStream inputStream = null;
        try {
            if (asset != null) {
                inputStream = context.getAssets().open(asset);
            } else {
                inputStream = context.getResources().openRawResource(rawRes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    private static String readStream(InputStream inputStream) {
        if (inputStream == null) return null;

        int totalRead = 0;
        byte[] readBuffer = readBufferLocal.get();
        if (readBuffer == null) {
            readBuffer = new byte[64 * 1024];
            readBufferLocal.set(readBuffer);
        }
        try {
            int readLen;
            byte[] buffer = bufferLocal.get();
            if (buffer == null) {
                buffer = new byte[4096];
                bufferLocal.set(buffer);
            }
            while ((readLen = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                if (readBuffer.length < totalRead + readLen) {
                    byte[] newBuffer = new byte[readBuffer.length * 2];
                    System.arraycopy(readBuffer, 0, newBuffer, 0, totalRead);
                    readBuffer = newBuffer;
                    readBufferLocal.set(readBuffer);
                }
                if (readLen > 0) {
                    System.arraycopy(buffer, 0, readBuffer, totalRead, readLen);
                    totalRead += readLen;
                }
            }
        } catch (Throwable e) {
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (Throwable ignore) {
            }
        }

        return new String(readBuffer, 0, totalRead);
    }
}
