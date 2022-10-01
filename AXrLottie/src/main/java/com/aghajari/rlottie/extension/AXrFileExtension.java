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


package com.aghajari.rlottie.extension;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * AXrFileExtension specifies which type of files can be used in lottie.
 * <p>
 * <code>AXrLottie.addFileExtension(AXrFileExtension);</code>
 */
public abstract class AXrFileExtension {

    public final String extension;

    public AXrFileExtension(String extension) {
        if (TextUtils.isEmpty(extension)) {
            throw new IllegalArgumentException("extension can not be null!");
        }
        this.extension = extension;
    }

    public String tempExtension() {
        return ".temp" + extension;
    }

    public File toFile(String cache, InputStream stream, boolean fromNetwork) throws IOException {
        return null;
    }

    public File toFile(String cache, File input, boolean fromNetwork) throws IOException {
        if (willReadStream()) {
            return toFile(cache, new FileInputStream(input), fromNetwork);
        } else {
            return input;
        }
    }

    public boolean willReadStream() {
        return false;
    }

    public boolean canParseContent(String contentType) {
        return false;
    }

    public boolean canParseFile(File file) {
        return canParseFile(file.getAbsolutePath());
    }

    public boolean canParseFile(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(extension);
    }

    @Override
    public String toString() {
        return extension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AXrFileExtension that = (AXrFileExtension) o;

        return Objects.equals(extension, that.extension);
    }
}
