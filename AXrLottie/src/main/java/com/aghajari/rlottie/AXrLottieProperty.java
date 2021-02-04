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
    final PropertyType type;

    private enum PropertyType {
        FillColor,
        FillOpacity,
        StrokeColor,
        StrokeOpacity,
        StrokeWidth,
        TrAnchor,
        TrOpacity,
        TrPosition,
        TrRotation,
        TrScale
    }

    final DynamicProperty<?> dynamicProperty;

    public interface DynamicProperty<T> {
        T getValue(int frame);
    }

    final int intValue;
    final float floatValue, floatValue2;

    AXrLottieProperty(PropertyType type, int value) {
        this.type = type;
        this.intValue = value;
        this.dynamicProperty = null;
        this.floatValue = 0;
        this.floatValue2 = 0;
    }

    AXrLottieProperty(PropertyType type, float value) {
        this.type = type;
        this.floatValue = value;
        this.dynamicProperty = null;
        this.intValue = 0;
        this.floatValue2 = 0;
    }

    AXrLottieProperty(PropertyType type, float value1, float value2) {
        this.type = type;
        this.floatValue = value1;
        this.floatValue2 = value2;
        this.dynamicProperty = null;
        this.intValue = 0;
    }

    AXrLottieProperty(PropertyType type, DynamicProperty<?> dynamicProperty) {
        this.type = type;
        this.dynamicProperty = dynamicProperty;
        this.intValue = 0;
        this.floatValue = 0;
        this.floatValue2 = 0;
    }

    /* Color property of Fill object */
    public static AXrLottieProperty fillColor(int color) {
        return new AXrLottieProperty(PropertyType.FillColor, color);
    }

    /* Dynamic color property of Fill object */
    public static AXrLottieProperty dynamicFillColor(DynamicProperty<Integer> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.FillColor, dynamicProperty);
    }

    /* Opacity property of Fill object, [ 0 .. 100] */
    public static AXrLottieProperty fillOpacity(float value) {
        return new AXrLottieProperty(PropertyType.FillOpacity, value);
    }

    /* Dynamic opacity property of Fill object, [ 0 .. 100] */
    public static AXrLottieProperty dynamicFillOpacity(DynamicProperty<Float> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.FillOpacity, dynamicProperty);
    }

    /* Color property of Stroke object */
    public static AXrLottieProperty strokeColor(int color) {
        return new AXrLottieProperty(PropertyType.StrokeColor, color);
    }

    /* Dynamic color property of Stroke object */
    public static AXrLottieProperty dynamicStrokeColor(DynamicProperty<Integer> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.StrokeColor, dynamicProperty);
    }

    /* Opacity property of Stroke object, [ 0 .. 100] */
    public static AXrLottieProperty strokeOpacity(float value) {
        return new AXrLottieProperty(PropertyType.StrokeOpacity, value);
    }

    /* Dynamic opacity property of Stroke object, [ 0 .. 100] */
    public static AXrLottieProperty dynamicStrokeOpacity(DynamicProperty<Float> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.StrokeOpacity, dynamicProperty);
    }

    /* Stroke with property of Stroke object */
    public static AXrLottieProperty strokeWidth(float value) {
        return new AXrLottieProperty(PropertyType.StrokeWidth, value);
    }

    /* Dynamic stroke with property of Stroke object */
    public static AXrLottieProperty dynamicStrokeWidth(DynamicProperty<Float> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.StrokeWidth, dynamicProperty);
    }

    /* Transform Scale property of Layer and Group object, range[0 .. 360] in degrees*/
    public static AXrLottieProperty trRotation(float value) {
        return new AXrLottieProperty(PropertyType.TrRotation, value);
    }

    /* Dynamic transform Scale property of Layer and Group object, range[0 .. 360] in degrees*/
    public static AXrLottieProperty dynamicTrRotation(DynamicProperty<Float> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.TrRotation, dynamicProperty);
    }

    /* Transform Opacity property of Layer and Group object, [ 0 .. 100] */
    public static AXrLottieProperty trOpacity(float value) {
        return new AXrLottieProperty(PropertyType.TrOpacity, value);
    }

    /* Dynamic transform Opacity property of Layer and Group object, [ 0 .. 100] */
    public static AXrLottieProperty dynamicTrOpacity(DynamicProperty<Float> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.TrOpacity, dynamicProperty);
    }

    /* Transform Anchor property of Layer and Group object */
    public static AXrLottieProperty trAnchor(float x, float y) {
        return new AXrLottieProperty(PropertyType.TrAnchor, x, y);
    }

    /* Dynamic transform Anchor property of Layer and Group object */
    public static AXrLottieProperty dynamicTrAnchor(DynamicProperty<Float[]> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.TrAnchor, dynamicProperty);
    }

    /* Transform Position property of Layer and Group object */
    public static AXrLottieProperty trPosition(float x, float y) {
        return new AXrLottieProperty(PropertyType.TrPosition, x, y);
    }

    /* Dynamic transform Position property of Layer and Group object */
    public static AXrLottieProperty dynamicTrPosition(DynamicProperty<Float[]> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.TrPosition, dynamicProperty);
    }

    /* Transform Scale property of Layer and Group object, range[0 ..100] */
    public static AXrLottieProperty trScale(float w, float h) {
        return new AXrLottieProperty(PropertyType.TrScale, w, h);
    }

    /* Dynamic transform Scale property of Layer and Group object, range[0 ..100] */
    public static AXrLottieProperty dynamicTrScale(DynamicProperty<Float[]> dynamicProperty) {
        return new AXrLottieProperty(PropertyType.TrScale, dynamicProperty);
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
        if (property.dynamicProperty != null) {
            switch (property.type) {
                case FillColor:
                    AXrLottieNative.setDynamicLayerColor(ptr, layer, property.dynamicProperty);
                    break;
                case FillOpacity:
                    AXrLottieNative.setDynamicLayerFillOpacity(ptr, layer, property.dynamicProperty);
                    break;
                case StrokeColor:
                    AXrLottieNative.setDynamicLayerStrokeColor(ptr, layer, property.dynamicProperty);
                    break;
                case StrokeOpacity:
                    AXrLottieNative.setDynamicLayerStrokeOpacity(ptr, layer, property.dynamicProperty);
                    break;
                case StrokeWidth:
                    AXrLottieNative.setDynamicLayerStrokeWidth(ptr, layer, property.dynamicProperty);
                    break;
                case TrAnchor:
                    AXrLottieNative.setDynamicLayerTrAnchor(ptr, layer, property.dynamicProperty);
                    break;
                case TrOpacity:
                    AXrLottieNative.setDynamicLayerTrOpacity(ptr, layer, property.dynamicProperty);
                    break;
                case TrPosition:
                    AXrLottieNative.setDynamicLayerTrPosition(ptr, layer, property.dynamicProperty);
                    break;
                case TrRotation:
                    AXrLottieNative.setDynamicLayerTrRotation(ptr, layer, property.dynamicProperty);
                    break;
                case TrScale:
                    AXrLottieNative.setDynamicLayerTrScale(ptr, layer, property.dynamicProperty);
                    break;
            }
        } else {
            switch (property.type) {
                case FillColor:
                    AXrLottieNative.setLayerColor(ptr, layer, property.intValue);
                    break;
                case FillOpacity:
                    AXrLottieNative.setLayerFillOpacity(ptr, layer, property.floatValue);
                    break;
                case StrokeColor:
                    AXrLottieNative.setLayerStrokeColor(ptr, layer, property.intValue);
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
}
