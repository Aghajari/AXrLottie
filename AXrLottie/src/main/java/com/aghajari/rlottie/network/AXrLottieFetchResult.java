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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * The result of the operation of obtaining a Lottie animation
 */
public interface AXrLottieFetchResult extends Closeable {
    /**
     * @return Is the operation successful
     */
    boolean isSuccessful();

    /**
     * @return Received content stream
     */
    @NonNull
    InputStream bodyByteStream() throws IOException;

    /**
     * @return Type of content received
     */
    @Nullable
    String contentType();

    /**
     * @return Operation error
     */
    @Nullable
    String error();
}