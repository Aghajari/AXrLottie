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


public class AXrLottieProperty {
    PropertyType type;

    private enum PropertyType {
        Color,
        FillOpacity,
        StrokeOpacity,
        StrokeWidth,
        TrAnchor,
        TrOpacity,
        TrPosition,
        TrRotation,
        TrScale
    }

    int intValue;
    float floatValue, floatValue2;


    AXrLottieProperty(PropertyType type, int value) {
        this.type = type;
        this.intValue = value;
    }

    AXrLottieProperty(PropertyType type, float value) {
        this.type = type;
        this.floatValue = value;
    }

    AXrLottieProperty(PropertyType type, float value1, float value2) {
        this.type = type;
        this.floatValue = value1;
        this.floatValue2 = value2;
    }

    /* Color property of Fill object */
    public static AXrLottieProperty colorProperty(int color) {
        return new AXrLottieProperty(PropertyType.Color, color);
    }

    /* Opacity property of Fill object, [ 0 .. 100] */
    public static AXrLottieProperty fillOpacity(float value) {
        return new AXrLottieProperty(PropertyType.FillOpacity, value);
    }

    /* Opacity property of Stroke object, [ 0 .. 100] */
    public static AXrLottieProperty strokeOpacity(float value) {
        return new AXrLottieProperty(PropertyType.StrokeOpacity, value);
    }

    /* stroke with property of Stroke object */
    public static AXrLottieProperty strokeWidth(float value) {
        return new AXrLottieProperty(PropertyType.StrokeWidth, value);
    }

    /* Transform Scale property of Layer and Group object, range[0 .. 360] in degrees*/
    public static AXrLottieProperty transformRotation(float value) {
        return new AXrLottieProperty(PropertyType.TrRotation, value);
    }

    /* Transform Opacity property of Layer and Group object, [ 0 .. 100] */
    public static AXrLottieProperty transformOpacity(float value) {
        return new AXrLottieProperty(PropertyType.TrOpacity, value);
    }

    /* Transform Anchor property of Layer and Group object */
    public static AXrLottieProperty transformAnchor(float x, float y) {
        return new AXrLottieProperty(PropertyType.TrAnchor, x, y);
    }

    /* Transform Position property of Layer and Group object */
    public static AXrLottieProperty transformPosition(float x, float y) {
        return new AXrLottieProperty(PropertyType.TrPosition, x, y);
    }

    /* Transform Scale property of Layer and Group object, range[0 ..100] */
    public static AXrLottieProperty transformScale(float w, float h) {
        return new AXrLottieProperty(PropertyType.TrScale, w, h);
    }

    static class PropertyUpdate {
        AXrLottieProperty property;
        String layer;

        PropertyUpdate(AXrLottieProperty property, String layer) {
            this.property = property;
            this.layer = layer;
        }

        void apply(long ptr) {
            AXrLottieProperty.apply(ptr, layer, property);
        }
    }

    private static void apply(long ptr, String layer, AXrLottieProperty property) {
        switch (property.type) {
            case Color:
                AXrLottieNative.setLayerColor(ptr, layer, property.intValue);
                break;
            case FillOpacity:
                AXrLottieNative.setLayerFillOpacity(ptr, layer, property.floatValue);
                break;
            case StrokeOpacity:
                AXrLottieNative.setLayerStrokeOpacity(ptr, layer, property.floatValue);
                break;
            case StrokeWidth:
                AXrLottieNative.setLayerStrokeWidth(ptr, layer, property.floatValue);
                break;
            case TrAnchor:
                AXrLottieNative.setLayerTrAnchor(ptr, layer, property.floatValue, property.floatValue2);
                break;
            case TrOpacity:
                AXrLottieNative.setLayerTrOpacity(ptr, layer, property.floatValue);
                break;
            case TrPosition:
                AXrLottieNative.setLayerTrPosition(ptr, layer, property.floatValue, property.floatValue2);
                break;
            case TrRotation:
                AXrLottieNative.setLayerTrRotation(ptr, layer, property.floatValue);
                break;
            case TrScale:
                AXrLottieNative.setLayerTrScale(ptr, layer, property.floatValue, property.floatValue2);
                break;
        }
    }
}
