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

import androidx.annotation.WorkerThread;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.extension.AXrFileExtension;
import com.aghajari.rlottie.extension.JsonFileExtension;

import java.io.File;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;

/**
 * Helper class to read InputStream
 */
public class AXrStreamParser {

    @WorkerThread
    public static AXrLottieResult<File> parseStream(InputStream inputStream, String contentType, String name, boolean fromNetwork) {
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
                        parsed = fileExtension.toFile(name, inputStream, fromNetwork) != null;
                    } else {
                        File input = AXrLottie.getLottieCacheManager().writeTempCacheFile(name, inputStream, fileExtension, fromNetwork);
                        parsed = fileExtension.toFile(name, input, fromNetwork) != null;
                        if (!parsed && input != null && input.exists())
                            input.delete();
                    }
                }
                if (parsed) break;
            }
            if (!parsed) {
                AXrLottie.getLottieCacheManager().writeTempCacheFile(name, inputStream, JsonFileExtension.JSON, fromNetwork);
            }

            File file = AXrLottie.getLottieCacheManager().loadTempFile(name, fromNetwork);
            return new AXrLottieResult<>(file);
        } catch (Exception e) {
            return new AXrLottieResult<>(e);
        }
    }

    @WorkerThread
    public static AXrLottieResult<File> parseStream(InputStream inputStream, AXrFileExtension extension, String name, boolean fromNetwork) {
        try {
            boolean parsed;
            if (extension.willReadStream()) {
                parsed = extension.toFile(name, inputStream, fromNetwork) != null;
            } else {
                File input = AXrLottie.getLottieCacheManager().writeTempCacheFile(name, inputStream, extension, fromNetwork);
                parsed = extension.toFile(name, input, fromNetwork) != null;
                if (!parsed && input != null && input.exists())
                    input.delete();
            }
            if (!parsed) {
                return new AXrLottieResult<>(new InvalidPropertiesFormatException("couldn't read " + name + " as " + extension.extension));
            }

            File file = AXrLottie.getLottieCacheManager().loadTempFile(name, fromNetwork);
            return new AXrLottieResult<>(file);
        } catch (Exception e) {
            return new AXrLottieResult<>(e);
        }
    }
}
