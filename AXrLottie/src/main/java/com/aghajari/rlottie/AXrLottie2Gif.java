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
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

import java.io.File;

public class AXrLottie2Gif {
    private static DispatchQueuePool runnableQueue = null;

    public interface Lottie2GifListener {
        void onStarted();

        void onProgress(int frame, int totalFrame);

        void onFinished();
    }

    private boolean running;
    private boolean successful;
    private boolean destroyed = false;
    private final Builder builder;
    private int mFrame, mTotalFrame;

    private final Lottie2GifListener listener = new Lottie2GifListener() {
        @Override
        public void onStarted() {
            running = true;
            if (builder.listener != null) builder.listener.onStarted();
        }

        @Override
        public void onProgress(int frame, int totalFrame) {
            mFrame = frame;
            mTotalFrame = totalFrame;
            if (builder.listener != null) builder.listener.onProgress(frame, totalFrame);
        }

        @Override
        public void onFinished() {
            running = false;

            if (builder.destroyable) destroy();
            if (builder.listener != null) builder.listener.onFinished();
        }
    };

    AXrLottie2Gif(Builder builder) {
        if (runnableQueue == null)
            runnableQueue = new DispatchQueuePool(2);
        this.builder = builder;
        build();
    }

    public boolean buildAgain() {
        if (isRunning()) return false;
        if (destroyed) {
            throw new RuntimeException("can't build a destroyable lottie again!");
        }
        build();
        return successful;
    }

    private Bitmap bitmap = null;

    Runnable converter = new Runnable() {
        @Override
        public void run() {
            if (bitmap == null) {
                try {
                    bitmap = Bitmap.createBitmap(builder.w, builder.h, Bitmap.Config.ARGB_8888);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            if (bitmap != null) {
                successful = AXrLottieNative.lottie2gif(builder.lottie, bitmap, builder.w, builder.h, bitmap.getRowBytes(),
                        builder.bgColor, builder.path.getAbsolutePath(),
                        builder.delay, builder.bitDepth, builder.dither, builder.frameStart, builder.frameEnd, listener);
            } else {
                successful = false;
            }

            if (!successful && builder.destroyable) destroy();
        }
    };

    private void build() {
        if (builder.async) {
            runnableQueue.execute(converter);
        } else {
            converter.run();
        }
    }

    private void destroy() {
        destroyed = true;
        AXrLottieNative.destroy(builder.lottie);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public Builder getBuilder() {
        return builder;
    }

    public int getCurrentFrame() {
        return mFrame;
    }

    public int getTotalFrame() {
        return mTotalFrame;
    }

    public File getGifPath() {
        return builder.path;
    }

    public static Builder create(@NonNull AXrLottieDrawable lottie) {
        return new Builder(lottie);
    }

    public static Builder create(long lottie) {
        return new Builder(lottie);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AXrLottie2Gif)) return false;
        AXrLottie2Gif c = (AXrLottie2Gif) o;
        if (c.getBuilder() == null) return false;
        return (c.getGifPath().equals(getGifPath()) && c.getBuilder().lottie == builder.lottie);
    }

    @Override
    public String toString() {
        return getGifPath().getAbsolutePath();
    }

    public static class Builder {
        long lottie;
        int w, h;
        int bgColor = Color.WHITE;
        int delay = 2;
        Lottie2GifListener listener = null;
        File path;
        boolean destroyable = false;
        boolean async = true;
        int bitDepth = 8;
        boolean dither = false;
        int frameStart = 0;
        int frameEnd = -1;
        boolean cancelable = false;

        public Builder(@NonNull AXrLottieDrawable animation) {
            this.lottie = animation.getNativePtr();
            float density = 1;
            if (AXrLottie.applicationContext != null)
                density = AXrLottie.applicationContext.getResources().getDisplayMetrics().density;
            setSize((int) (animation.getMinimumWidth() / density), (int) (animation.getMinimumHeight() / density));
        }

        public Builder(long ptr) {
            this.lottie = ptr;
            setSize(200, 200);
        }

        public Builder setLottieAnimation(@NonNull AXrLottieDrawable animation) {
            this.lottie = animation.getNativePtr();
            return this;
        }

        public Builder setLottieAnimation(long ptr) {
            this.lottie = ptr;
            return this;
        }

        /**
         * set the output gif background color
         */
        public Builder setBackgroundColor(int bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        /**
         * set the output gif width and height
         */
        public Builder setSize(@Px int width, @Px int height) {
            this.w = width;
            this.h = height;
            return this;
        }

        public Builder setListener(Lottie2GifListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * The delay value is the time between frames in hundredths of a second
         */
        public Builder setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        /**
         * set the output gif path
         */
        public Builder setOutputPath(@NonNull File gif) {
            this.path = gif;
            return this;
        }

        /**
         * set the output gif path
         */
        public Builder setOutputPath(@NonNull String gif) {
            this.path = new File(gif);
            return this;
        }

        /**
         * destroy lottie when gif created
         */
        public Builder setDestroyable(boolean destroyable) {
            this.destroyable = destroyable;
            return this;
        }

        public Builder setBackgroundTask(boolean enabled) {
            async = enabled;
            return this;
        }

        /**
         * Implements Floyd-Steinberg dithering, writes palette value to alpha
         */
        public Builder setDithering(boolean enabled) {
            dither = enabled;
            return this;
        }

        public Builder setBitDepth(int bit) {
            bitDepth = bit;
            return this;
        }

        public Builder setFrameStartAt(int frameStart) {
            this.frameStart = frameStart;
            return this;
        }

        public Builder setFrameEndAt(int frameEnd) {
            this.frameEnd = frameEnd;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public AXrLottie2Gif build() {
            if (path == null) {
                throw new RuntimeException("output gif path can't be null!");
            }
            if (w <= 0 || h <= 0) {
                throw new RuntimeException("output gif width and height must be > 0");
            }

            return new AXrLottie2Gif(this);
        }
    }

}
