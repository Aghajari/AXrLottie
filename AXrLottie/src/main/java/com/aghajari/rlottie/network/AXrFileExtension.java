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

import com.aghajari.rlottie.AXrLottie;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helpers for known Lottie downloader file types.
 */
public abstract class AXrFileExtension {

    public final String extension;

    public AXrFileExtension(String extension) {
        this.extension = extension;
    }

    public String tempExtension() {
        return ".temp" + extension;
    }

    public boolean canParseContent(String contentType) {
        return false;
    }

    public File saveAsTempFile(String url, InputStream stream) throws IOException {
        return AXrLottie.getLottieCacheManager().writeTempCacheFile(url, stream, this);
    }

    @Override
    public String toString() {
        return extension;
    }
}
