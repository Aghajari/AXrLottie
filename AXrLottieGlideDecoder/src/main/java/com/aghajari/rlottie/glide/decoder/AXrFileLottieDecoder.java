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


/**
 * An {@link ResourceDecoder} that decodes
 * {@link AXrLottieDrawable} from {@link File} data
 */
public class AXrFileLottieDecoder implements ResourceDecoder<File, AXrLottieDrawable> {

    @Override
    public boolean handles(@NonNull File file, @NonNull Options options) throws IOException {
        return AXrLottieGlideLibraryModule.handles(options);
    }

    @Nullable
    @Override
    public Resource<AXrLottieDrawable> decode(@NonNull File file, int width, int height, @NonNull Options options) throws IOException {
        AXrLottieResult<File> result = AXrLottieGlideLibraryModule.parseFile(file,options);
        return AXrLottieGlideLibraryModule.createDrawable(result,width,height,options);
    }
}