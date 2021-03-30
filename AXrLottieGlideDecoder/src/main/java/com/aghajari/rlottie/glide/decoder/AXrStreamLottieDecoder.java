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


package com.aghajari.rlottie.glide.decoder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.rlottie.AXrLottieDrawable;
import com.aghajari.rlottie.decoder.AXrLottieResult;
import com.aghajari.rlottie.glide.AXrLottieGlideLibraryModule;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * An {@link com.bumptech.glide.load.ResourceDecoder} that decodes
 * {@link AXrLottieDrawable} from {@link java.io.InputStream} data
 */
public class AXrStreamLottieDecoder implements ResourceDecoder<InputStream, AXrLottieDrawable> {

    @Override
    public boolean handles(@NonNull InputStream inputStream, @NonNull Options options) throws IOException {
        return AXrLottieGlideLibraryModule.handles(options);
    }

    @Nullable
    @Override
    public Resource<AXrLottieDrawable> decode(@NonNull InputStream inputStream, int width, int height, @NonNull Options options) throws IOException {
        AXrLottieResult<File> result = AXrLottieGlideLibraryModule.parseStream(inputStream,options);
        return AXrLottieGlideLibraryModule.createDrawable(result,width,height,options);
    }
}