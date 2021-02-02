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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * Helpers for known Lottie downloader file types.
 */
public abstract class FileExtension {

    public static FileExtension ZIP() {
        return new FileExtension(".zip") {

            @Override
            public boolean canParseContent(String contentType) {
                return ZipCompositionFactory.isZipContent(contentType);
            }

            public File fromZipStream(File file, File output, ZipInputStream stream) {
                return ZipCompositionFactory.fromZipStreamSyncInternal(file, output, stream);
            }

            @Override
            public File saveAsTempFile(Context context, String cacheName, InputStream stream) throws IOException {
                File file = NetworkCache.writeTempCacheFile(context, cacheName, stream, FileExtension.ZIP());
                file = fromZipStream(file,
                        NetworkCache.getCachedFile(context, NetworkCache.filenameForUrl(cacheName, FileExtension.JSON(), true)),
                        new ZipInputStream(new FileInputStream(file)));
                return file;
            }
        };
    }

    public static FileExtension JSON() {
        return new FileExtension(".json") {
            @Override
            public boolean canParseContent(String contentType) {
                return contentType.toLowerCase().contains("application/json");
            }
        };
    }

    public final String extension;

    public FileExtension(String extension) {
        this.extension = extension;
    }

    public String tempExtension() {
        return ".temp" + extension;
    }

    public boolean canParseContent(String contentType) {
        return false;
    }

    public File saveAsTempFile(Context context, String cacheName, InputStream stream) throws IOException {
       return NetworkCache.writeTempCacheFile(context,cacheName, stream, FileExtension.JSON());
    }

    @Override
    public String toString() {
        return extension;
    }
}
