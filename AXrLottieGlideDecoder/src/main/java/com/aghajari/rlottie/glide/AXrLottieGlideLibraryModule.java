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


package com.aghajari.rlottie.glide;

import android.content.Context;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieDrawable;
import com.aghajari.rlottie.decoder.AXrFileReader;
import com.aghajari.rlottie.decoder.AXrLottieResult;
import com.aghajari.rlottie.decoder.AXrStreamParser;
import com.aghajari.rlottie.glide.decoder.AXrByteBufferLottieDecoder;
import com.aghajari.rlottie.glide.decoder.AXrFileLottieDecoder;
import com.aghajari.rlottie.glide.decoder.AXrFileStreamLottieDecoder;
import com.aghajari.rlottie.glide.decoder.AXrLottieDrawableEncoder;
import com.aghajari.rlottie.glide.decoder.AXrLottieDrawableResource;
import com.aghajari.rlottie.glide.decoder.AXrStreamLottieDecoder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.module.LibraryGlideModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;


@GlideModule
public class AXrLottieGlideLibraryModule extends LibraryGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry
                .prepend(ByteBuffer.class, AXrLottieDrawable.class, new AXrByteBufferLottieDecoder())
                .prepend(InputStream.class, AXrLottieDrawable.class, new AXrStreamLottieDecoder())
                .prepend(FileInputStream.class, AXrLottieDrawable.class, new AXrFileStreamLottieDecoder())
                .prepend(File.class, AXrLottieDrawable.class, new AXrFileLottieDecoder())
                .prepend(AXrLottieDrawable.class, new AXrLottieDrawableEncoder());
    }

    public static AXrLottieDrawableResource createDrawable(AXrLottieResult<File> result, int w, int h, Options options) {
        File file = result.getValue();
        if (file == null)
            return null;

        int size = get(options, AXrLottieGlideOptions.SIZE);

        AXrLottieDrawable.Builder builder = AXrLottieDrawable.fromFile(file);
        builder.apply(options.get(AXrLottieGlideOptions.OPTIONS));
        if (size > 0)
            builder.setSize(size, size);
        else
            builder.setSize(w, h);

        builder.setAutoStart(get(options, AXrLottieGlideOptions.AUTO_START));
        return new AXrLottieDrawableResource(builder.build());
    }

    public static boolean handles(Options options) {
        return get(options, AXrLottieGlideOptions.ENABLED) &&
                options.get(AXrLottieGlideOptions.NAME) != null;
    }

    public static AXrLottieResult<File> parseStream(InputStream stream, Options options) {
        return AXrStreamParser.parseStream(stream,
                AXrLottie.getSupportedFileExtensions().get(
                        get(options, AXrLottieGlideOptions.EXTENSION).toLowerCase()),
                options.get(AXrLottieGlideOptions.NAME),
                get(options, AXrLottieGlideOptions.NETWORK));
    }

    public static AXrLottieResult<File> parseFile(File file, Options options) {
        return new AXrLottieResult<>(AXrFileReader.fromFile(file,
                get(options, AXrLottieGlideOptions.NETWORK),
                options.get(AXrLottieGlideOptions.NAME)));
    }

    private static <T> T get(Options options, Option<T> key) {
        T value = options.get(key);
        if (value == null)
            value = key.getDefaultValue();

        return value;
    }
}