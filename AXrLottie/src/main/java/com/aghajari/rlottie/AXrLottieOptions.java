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


package com.aghajari.rlottie;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class AXrLottieOptions {

    public AXrLottieOptions() {
        apply(AXrLottie.getDefaultOptions());
    }

    static final int DEFAULT = -100;

    String cacheName;
    int w = DEFAULT, h = DEFAULT;
    boolean cache = true;
    boolean limitFps = false;
    boolean startDecode = true;
    List<AXrLottieProperty.PropertyUpdate> properties = null;
    int customEndFrame = DEFAULT;
    int customStartFrame = DEFAULT;
    int repeatMode = DEFAULT;
    int autoRepeat = DEFAULT;
    AXrLottieDrawable.OnFrameChangedListener listener = null;
    AXrLottieDrawable.OnFrameRenderListener render = null;
    AXrLottieDrawable.OnLottieLoaderListener loaderListener = null;
    boolean autoStart;
    AXrLottieMarker selectedMarker = null;
    float speed = -1;


    /**
     * will be used to cache the JSON string data and compare drawables.
     */
    public AXrLottieOptions setCacheName(String cacheName) {
        if (TextUtils.isEmpty(cacheName)) {
            if (TextUtils.isEmpty(cacheName))
                throw new NullPointerException("lottie name (cacheName) can not be null!");
            else
                return this;
        }
        this.cacheName = cacheName;
        return this;
    }

    /**
     * set lottie min width and height
     */
    public AXrLottieOptions setSize(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new RuntimeException("lottie width and height must be > 0");
        }
        this.w = w;
        this.h = h;
        return this;
    }

    /**
     * set lottie cache enabled
     */
    public AXrLottieOptions setCacheEnabled(boolean enabled) {
        this.cache = enabled;
        return this;
    }

    public AXrLottieOptions setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    /**
     * set lottie frame rate limit
     */
    public AXrLottieOptions setFpsLimit(boolean limitFps) {
        this.limitFps = limitFps;
        return this;
    }

    public AXrLottieOptions setAllowDecodeSingleFrame(boolean startDecode) {
        this.startDecode = startDecode;
        return this;
    }

    /**
     * Sets property value for the specified layer. layer can resolve
     * to multiple contents. In that case, the callback's value will apply to all of them.
     * <p>
     * keyPath should contain object names separated by (.) and can handle globe(**) or wildchar(*).
     */
    public AXrLottieOptions addLayerProperty(String keyPath, AXrLottieProperty property) {
        if (properties == null) properties = new ArrayList<>();
        properties.add(new AXrLottieProperty.PropertyUpdate(property, keyPath));
        return this;
    }

    public AXrLottieOptions setCustomEndFrame(int customEndFrame) {
        this.customEndFrame = customEndFrame;
        return this;
    }

    public AXrLottieOptions setCustomStartFrame(int customStartFrame) {
        this.customStartFrame = customStartFrame;
        return this;
    }

    public AXrLottieOptions setSelectedMarker(AXrLottieMarker marker) {
        this.selectedMarker = marker;
        return this;
    }

    /**
     * Set auto repeat count
     *
     * @see AXrLottieDrawable#AUTO_REPEAT_INFINITE
     */
    public AXrLottieOptions setAutoRepeat(int repeatCount) {
        autoRepeat = repeatCount;
        return this;
    }

    /**
     * Enable infinite auto repeat
     *
     * @see AXrLottieDrawable#setAutoRepeat(int)
     */
    public AXrLottieOptions setAutoRepeat(boolean enabled) {
        return setAutoRepeat(enabled ? AXrLottieDrawable.AUTO_REPEAT_INFINITE : 0);
    }

    /**
     * Set repeat mode
     *
     * @see AXrLottieDrawable#REPEAT_MODE_RESTART
     * @see AXrLottieDrawable#REPEAT_MODE_REVERSE
     */
    public AXrLottieOptions setAutoRepeatMode(int autoRepeatMode) {
        this.repeatMode = autoRepeatMode;
        return this;
    }

    public AXrLottieOptions setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return this;
    }

    public AXrLottieOptions setOnFrameChangedListener(AXrLottieDrawable.OnFrameChangedListener listener) {
        this.listener = listener;
        return this;
    }

    public AXrLottieOptions setOnFrameRenderListener(AXrLottieDrawable.OnFrameRenderListener render) {
        this.render = render;
        return this;
    }

    public AXrLottieOptions setOnLottieLoaderListener(AXrLottieDrawable.OnLottieLoaderListener loaderListener) {
        this.loaderListener = loaderListener;
        return this;
    }

    public AXrLottieOptions apply(AXrLottieOptions options) {
        if (options == null || options == this) return this;

        cacheName = options.cacheName;
        w = options.w;
        h = options.h;
        cache = options.cache;
        limitFps = options.limitFps;
        startDecode = options.startDecode;
        properties = options.properties;
        customEndFrame = options.customEndFrame;
        customStartFrame = options.customStartFrame;
        repeatMode = options.repeatMode;
        autoRepeat = options.autoRepeat;
        listener = options.listener;
        render = options.render;
        loaderListener = options.loaderListener;
        autoStart = options.autoStart;
        selectedMarker = options.selectedMarker;
        speed = options.speed;

        return this;
    }

    public AXrLottieDrawable build() {
        throw new RuntimeException("Can't build an AXrLottieDrawable from AXrLottieOptions!");
    }
}
