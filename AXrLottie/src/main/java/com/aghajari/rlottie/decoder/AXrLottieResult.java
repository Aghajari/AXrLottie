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


package com.aghajari.rlottie.decoder;

import androidx.annotation.Nullable;

import java.util.Arrays;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * Contains class to hold the resulting value of an async task or an exception if it failed.
 * Either value or exception will be non-null.
 */
public final class AXrLottieResult<V> {

    @Nullable
    private final V value;
    @Nullable
    private final Throwable exception;

    public AXrLottieResult(V value) {
        this.value = value;
        exception = null;
    }

    public AXrLottieResult(Throwable exception) {
        this.exception = exception;
        value = null;
    }

    @Nullable
    public V getValue() {
        return value;
    }

    @Nullable
    public Throwable getException() {
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AXrLottieResult)) {
            return false;
        }
        AXrLottieResult<?> that = (AXrLottieResult<?>) o;
        if (getValue() != null && getValue().equals(that.getValue())) {
            return true;
        }
        if (getException() != null && that.getException() != null) {
            return getException().toString().equals(getException().toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{getValue(), getException()});
    }
}

