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


package com.aghajari.rlottie;

import android.graphics.Bitmap;

class AXrLottieNative {
    /** AXrLottie */
    public static native void configureModelCacheSize(int cacheSize);

    /** AXrLottieDrawable */
    public static native long create(String src, int w, int h, int[] params, boolean precache, boolean limitFps);
    public static native long createWithJson(String json, String name, int[] params);
    public static native void destroy(long ptr);
    public static native void createCache(long ptr, int w, int h);
    public static native int getFrame(long ptr, int frame, Bitmap bitmap, int w, int h, int stride);

    /** Layer & Marker */
    public static native int getMarkersCount(long ptr);
    public static native String[] getMarkerData(long ptr, int index);
    public static native int getLayersCount(long ptr);
    public static native String[] getLayerData(long ptr, int index);

    /** Properties */
    public static native void setLayerColor(long ptr, String layer, int color);
    public static native void setLayerStrokeColor(long ptr, String layer, int color);
    public static native void setLayerFillOpacity(long ptr, String layer, float color);
    public static native void setLayerStrokeOpacity(long ptr, String layer, float value);
    public static native void setLayerStrokeWidth(long ptr, String layer, float value);
    public static native void setLayerTrRotation(long ptr, String layer, float value);
    public static native void setLayerTrOpacity(long ptr, String layer, float value);
    public static native void setLayerTrAnchor(long ptr, String layer, float x, float y);
    public static native void setLayerTrPosition(long ptr, String layer, float x, float y);
    public static native void setLayerTrScale(long ptr, String layer, float w, float h);

    /** Dynamic properties */
    public static native void setDynamicLayerColor(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerStrokeColor(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerFillOpacity(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerStrokeOpacity(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerStrokeWidth(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerTrRotation(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerTrOpacity(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerTrAnchor(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerTrPosition(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);
    public static native void setDynamicLayerTrScale(long ptr, String layer, AXrLottieProperty.DynamicProperty<?> dynamicProperty);

    /** Lottie2Gif */
    public static native boolean lottie2gif(long ptr, Bitmap bitmap, int w, int h, int stride, int bgColor, String gifPath, int delay, int bitDepth, boolean dither, int frameStart, int frameEnd, AXrLottie2Gif.Lottie2GifListener listener);
}
