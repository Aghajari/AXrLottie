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

    AXrLottieLayerInfo(String[] data) {
        try {
            if (data != null) {
                name = data[0];
                inFrame = Integer.parseInt(data[1]);
                outFrame = Integer.parseInt(data[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public String toString() {
        return "AXrLottieLayerInfo{" +
                "name='" + name + '\'' +
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
        if (name == null) return layerInfo.name == null;
        return name.equals(layerInfo.name);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + inFrame;
        result = 31 * result + outFrame;
        return result;
    }
}
