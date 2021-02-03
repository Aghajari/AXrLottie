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

import com.aghajari.rlottie.AXrLottie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class ZipFileExtension extends AXrFileExtension {
    public static final ZipFileExtension ZIP = new ZipFileExtension();

    ZipFileExtension() {
        super(".zip");
    }

    @Override
    public boolean canParseContent(String contentType) {
        return ZipCompositionFactory.isZipContent(contentType);
    }

    public File fromZipStream(File file, File output, ZipInputStream stream) {
        return ZipCompositionFactory.fromZipStreamSyncInternal(file, output, stream);
    }

    @Override
    public File saveAsTempFile(String url, InputStream stream) throws IOException {
        File file = AXrLottie.getLottieCacheManager().writeTempCacheFile(url, stream, this);
        file = fromZipStream(file,
                AXrLottie.getLottieCacheManager().getCachedFile(url, JsonFileExtension.JSON, true),
                new ZipInputStream(new FileInputStream(file)));
        return file;
    }
}
