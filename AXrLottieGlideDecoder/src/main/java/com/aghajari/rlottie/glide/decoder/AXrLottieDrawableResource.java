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

import com.aghajari.rlottie.AXrLottieDrawable;
import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.engine.Resource;

public class AXrLottieDrawableResource implements Resource<AXrLottieDrawable>,Initializable {

    AXrLottieDrawable drawable;

    public AXrLottieDrawableResource(AXrLottieDrawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public Class<AXrLottieDrawable> getResourceClass() {
        return AXrLottieDrawable.class;
    }

    @NonNull
    @Override
    public AXrLottieDrawable get() {
        return drawable;
    }

    @Override
    public int getSize() {
        return drawable.getMinimumWidth();
    }

    @Override
    public void recycle() {
        drawable.stop();
        drawable.recycle();
    }

    @Override
    public void initialize() {
    }
}