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

public class AXrLottieLayerInfo {

    private String name;
    private int inFrame;
    private int outFrame;
    private LayerType type;

    public enum LayerType {
        UNKNOWN(-1),
        PRECOM(0),
        SOLID(1),
        IMAGE(2),
        NULL(3),
        SHAPE(4),
        TEXT(5);

        private int type;

        LayerType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }
    }

    AXrLottieLayerInfo(String[] data) {
        try {
            if (data != null) {
                name = data[0];
                inFrame = Integer.parseInt(data[1]);
                outFrame = Integer.parseInt(data[2]);

                try {
                    type = LayerType.values()[Integer.parseInt(data[3]) + 1]; // +1 for skipping UNKNOWN
                } catch (Exception ignore) {
                }
            }
        } catch (Exception ignore) {
        }

        if (type == null) type = LayerType.UNKNOWN;
    }

    public int getInFrame() {
        return inFrame;
    }

    public int getOutFrame() {
        return outFrame;
    }

    public String getName() {
        return name;
    }

    public LayerType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AXrLottieLayerInfo{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", inFrame=" + inFrame +
                ", outFrame=" + outFrame +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AXrLottieLayerInfo layerInfo = (AXrLottieLayerInfo) o;

        if (inFrame != layerInfo.inFrame) return false;
        if (outFrame != layerInfo.outFrame) return false;
        if (type != layerInfo.type) return false;
        if (name == null) return layerInfo.name == null;
        return name.equals(layerInfo.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + inFrame;
        result = 31 * result + outFrame;
        result = 31 * result + type.hashCode();
        return result;
    }
}
