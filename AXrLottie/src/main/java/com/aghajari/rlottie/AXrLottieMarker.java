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

/**
 * Markers exported form AE are used to describe a segment of an animation {comment/tag , startFrame, endFrame}
 * Marker can be use to divide a resource in to separate animations by tagging the segment with comment string ,
 * start frame and duration of that segment.
 */
public class AXrLottieMarker {

    private String marker = "";
    private int inFrame = -1;
    private int outFrame = -1;

    AXrLottieMarker(String[] data) {
        try {
            if (data != null) {
                marker = data[0];
                inFrame = Integer.parseInt(data[1]);
                outFrame = Integer.parseInt(data[2]);
            }
        } catch (Exception ignore) {
        }
    }

    public AXrLottieMarker(String marker, int inFrame, int outFrame) {
        this.marker = marker;
        this.inFrame = inFrame;
        this.outFrame = outFrame;
    }

    public int getInFrame() {
        return inFrame;
    }

    public int getOutFrame() {
        return outFrame;
    }

    public String getMarker() {
        return marker;
    }

    @Override
    public String toString() {
        return "AXrLottieMarker{" +
                "marker='" + marker + '\'' +
                ", inFrame=" + inFrame +
                ", outFrame=" + outFrame +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AXrLottieMarker markerInfo = (AXrLottieMarker) o;

        if (inFrame != markerInfo.inFrame) return false;
        if (outFrame != markerInfo.outFrame) return false;
        if (marker == null) return markerInfo.marker == null;
        return marker.equals(markerInfo.marker);
    }

    @Override
    public int hashCode() {
        int result = marker.hashCode();
        result = 31 * result + inFrame;
        result = 31 * result + outFrame;
        return result;
    }
}
