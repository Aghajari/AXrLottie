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


package com.aghajari.rlottie.extension;

import androidx.annotation.WorkerThread;

import com.aghajari.rlottie.AXrLottie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * GZipFileExtension
 * File Type                       : GZIP
 * File Type Extension             : gz
 * MIME Type                       : application/x-gzip
 */
public class GZipFileExtension extends AXrFileExtension {

    public GZipFileExtension() {
        super(".gz");
    }

    public GZipFileExtension(String extension) {
        super(extension);
    }

    @Override
    public boolean canParseContent(String contentType) {
        return contentType.toLowerCase().contains("application/octet-stream")
                || contentType.toLowerCase().contains("binary/octet-stream")
                || contentType.toLowerCase().contains("application/x-gzip");
    }

    @WorkerThread
    public static File toFile(InputStream stream, File output) {
        try (GZIPInputStream gis = new GZIPInputStream(stream);
             FileOutputStream fos = new FileOutputStream(output)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public boolean willReadStream() {
        return true;
    }

    @Override
    public File toFile(String cache, InputStream stream, boolean fromNetwork) {
        return toFile(stream,
                AXrLottie.getLottieCacheManager().getCachedFile(cache, JsonFileExtension.JSON, fromNetwork, true));
    }
}
