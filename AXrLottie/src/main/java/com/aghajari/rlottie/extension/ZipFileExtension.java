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

import com.aghajari.rlottie.AXrLottie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

/**
 * ZipFileExtension
 * File Type                       : Compressed Archive File
 * File Type Extension             : zip
 * MIME Type                       : application/zip
 */
public class ZipFileExtension extends AXrFileExtension {

    public static final ZipFileExtension ZIP = new ZipFileExtension();

    public ZipFileExtension() {
        super(".zip");
    }

    public ZipFileExtension(String extension) {
        super(extension);
    }

    @Override
    public boolean canParseContent(String contentType) {
        return ZipCompositionFactory.isZipContent(contentType);
    }

    public File fromZipStream(File file, File output, ZipInputStream stream) {
        return ZipCompositionFactory.fromZipStreamSyncInternal(file, output, stream);
    }

    @Override
    public File toFile(String cache, File input, boolean fromNetwork) throws IOException {
        return fromZipStream(input,
                AXrLottie.getLottieCacheManager().getCachedFile(cache, JsonFileExtension.JSON, fromNetwork, true),
                new ZipInputStream(new FileInputStream(input)));
    }

}
