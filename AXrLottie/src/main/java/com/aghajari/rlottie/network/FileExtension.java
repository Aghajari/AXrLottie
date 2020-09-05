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

/**
 * Helpers for known Lottie downloader file types.
 */
public class FileExtension {
    static FileExtension ZIP() {
        return new FileExtension(".zip");
    }

    static FileExtension JSON() {
        return new FileExtension(".json");
    }

    final String extension;

    public FileExtension(String extension) {
        this.extension = extension;
    }

    String tempExtension() {
        return ".temp" + extension;
    }

    @Override
    public String toString() {
        return extension;
    }


}
