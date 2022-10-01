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

import com.aghajari.rlottie.AXrLottieDrawable;
import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.engine.Resource;
import java.io.File;

/**
 * Writes the json of a {@link AXrLottieDrawable} to an {@link java.io.OutputStream}.
 */
public class AXrLottieDrawableEncoder implements ResourceEncoder<AXrLottieDrawable> {

    @Override
    public EncodeStrategy getEncodeStrategy(Options options) {
        return EncodeStrategy.SOURCE;
    }

    @Override
    public boolean encode(Resource<AXrLottieDrawable> data, File file, Options options) {
        AXrLottieDrawable drawable = data.get();
        if (file == null)
            return false;
        return drawable.exportJson(file);
    }
}