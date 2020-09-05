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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.aghajari.rlottie.network.AXrLottieNetworkFetcher;
import com.aghajari.rlottie.network.AXrLottieSimpleNetworkFetcher;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.aghajari.rlottie.AXrLottieNative.destroy;
import static com.aghajari.rlottie.AXrLottieNative.create;
import static com.aghajari.rlottie.AXrLottieNative.createCache;
import static com.aghajari.rlottie.AXrLottieNative.createWithJson;
import static com.aghajari.rlottie.AXrLottieNative.getFrame;

public class AXrLottieDrawable extends BitmapDrawable implements Animatable {

    private int width;
    private int height;
    private final int[] metaData = new int[3];
    private int timeBetweenFrames;
    private int customEndFrame = -1;
    private boolean playInDirectionOfCustomEndFrame;
    private int[] newReplaceColors;
    private int[] pendingReplaceColors;
    private ArrayList<AXrLottieProperty.PropertyUpdate> newPropertyUpdates = new ArrayList<>();
    private volatile ArrayList<AXrLottieProperty.PropertyUpdate> pendingPropertyUpdates = new ArrayList<>();

    private int autoRepeat = 1;
    private int autoRepeatPlayCount;

    private long lastFrameTime;
    private volatile boolean nextFrameIsLast;

    private Runnable cacheGenerateTask;
    private Runnable loadFrameTask;
    private volatile Bitmap renderingBitmap;
    private volatile Bitmap nextRenderingBitmap;
    private volatile Bitmap backgroundBitmap;
    private boolean waitingForNextTask;

    private CountDownLatch frameWaitSync;

    private boolean destroyWhenDone;
    private boolean decodeSingleFrame;
    private boolean singleFrameDecoded;
    private boolean forceFrameRedraw;
    private boolean applyingLayerColors;
    private int currentFrame;
    private boolean shouldLimitFps;

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private boolean applyTransformation;
    private final Rect dstRect = new Rect();
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private volatile boolean isRunning;
    private volatile boolean isRecycled;
    private volatile long nativePtr;
    private volatile long secondNativePtr;


    private boolean invalidateOnProgressSet;
    private boolean isInvalid;
    private boolean doNotRemoveInvalidOnFrameReady;

    private static DispatchQueuePool loadFrameRunnableQueue = new DispatchQueuePool(4);
    private static ThreadPoolExecutor lottieCacheGenerateQueue;

    private OnFrameChangedListener listener = null;
    private OnFrameRenderListener render = null;
    private OnLottieLoaderListener loaderListener = null;

    public interface OnFrameChangedListener {
        void onFrameChanged(AXrLottieDrawable drawable, int frame);
    }

    public interface OnFrameRenderListener {
        void onUpdate(AXrLottieDrawable drawable, int frame, long timeDiff, boolean force);

        Bitmap renderFrame(AXrLottieDrawable drawable, Bitmap bitmap, int frame);
    }

    public interface OnLottieLoaderListener {
        void onLoaded(AXrLottieDrawable drawable);
    }

    public void setOnFrameChangedListener(OnFrameChangedListener listener) {
        this.listener = listener;
    }

    public void setOnFrameRenderListener(OnFrameRenderListener listener) {
        this.render = listener;
    }

    public void setOnLottieLoaderListener(OnLottieLoaderListener listener) {
        this.loaderListener = listener;
    }

    private Runnable uiRunnableNoFrame = new Runnable() {
        @Override
        public void run() {
            loadFrameTask = null;
            decodeFrameFinishedInternal();
        }
    };

    private Runnable uiRunnableCacheFinished = new Runnable() {
        @Override
        public void run() {
            cacheGenerateTask = null;
            decodeFrameFinishedInternal();
        }
    };

    private Runnable uiRunnable = new Runnable() {
        @Override
        public void run() {
            singleFrameDecoded = true;
            invalidateInternal();
            decodeFrameFinishedInternal();
        }
    };

    private Runnable uiRunnableGenerateCache = new Runnable() {
        @Override
        public void run() {
            if (!isRecycled && !destroyWhenDone && nativePtr != 0) {
                lottieCacheGenerateQueue.execute(cacheGenerateTask = new Runnable() {
                    @Override
                    public void run() {
                        if (cacheGenerateTask == null) {
                            return;
                        }
                        createCache(nativePtr, width, height);
                        uiHandler.post(uiRunnableCacheFinished);
                    }
                });
            }
            decodeFrameFinishedInternal();
        }
    };

    private void checkRunningTasks() {
        if (cacheGenerateTask != null) {
            if (lottieCacheGenerateQueue.remove(cacheGenerateTask)) {
                cacheGenerateTask = null;
            }
        }
        if (nextRenderingBitmap != null && loadFrameTask != null) {
            loadFrameTask = null;
            nextRenderingBitmap = null;
        }
    }

    private void decodeFrameFinishedInternal() {
        if (destroyWhenDone) {
            checkRunningTasks();
            if (loadFrameTask == null && cacheGenerateTask == null && nativePtr != 0) {
                destroy(nativePtr);
                nativePtr = 0;
                if (secondNativePtr != 0) {
                    destroy(secondNativePtr);
                    secondNativePtr = 0;
                }
            }
        }
        if (nativePtr == 0 && secondNativePtr == 0) {
            recycleResources();
            return;
        }
        waitingForNextTask = true;

        //stop();
        scheduleNextGetFrame();
    }

    private void recycleResources() {
        if (renderingBitmap != null) {
            renderingBitmap.recycle();
            renderingBitmap = null;
        }
        if (backgroundBitmap != null) {
            backgroundBitmap.recycle();
            backgroundBitmap = null;
        }
    }

    private Runnable loadFrameRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecycled) {
                return;
            }
            if (nativePtr == 0) {
                if (frameWaitSync != null) {
                    frameWaitSync.countDown();
                }
                uiHandler.post(uiRunnableNoFrame);
                return;
            }
            if (backgroundBitmap == null) {
                try {
                    backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (backgroundBitmap != null) {
                try {
                    if (!pendingPropertyUpdates.isEmpty()) {
                        for (AXrLottieProperty.PropertyUpdate entry : pendingPropertyUpdates) {
                            entry.apply(nativePtr);
                        }
                        pendingPropertyUpdates.clear();
                    }
                } catch (Exception ignore) {
                }
                if (pendingReplaceColors != null) {
                    AXrLottieNative.replaceColors(nativePtr, pendingReplaceColors);
                    pendingReplaceColors = null;
                }
                try {
                    long ptrToUse = nativePtr;

                    int result = getFrame(ptrToUse, currentFrame, backgroundBitmap, width, height, backgroundBitmap.getRowBytes());
                    if (result == -1) {
                        uiHandler.post(uiRunnableNoFrame);
                        if (frameWaitSync != null) {
                            frameWaitSync.countDown();
                        }
                        return;
                    }
                    if (metaData[2] != 0) {
                        uiHandler.post(uiRunnableGenerateCache);
                        metaData[2] = 0;
                    }
                    nextRenderingBitmap = backgroundBitmap;
                    int framesPerUpdates = shouldLimitFps ? 2 : 1;

                    if (customEndFrame > 0 && playInDirectionOfCustomEndFrame) {
                        if (currentFrame > customEndFrame) {
                            if (currentFrame - framesPerUpdates > customEndFrame) {
                                currentFrame -= framesPerUpdates;
                                nextFrameIsLast = false;
                            } else {
                                nextFrameIsLast = true;
                            }
                        } else {
                            if (currentFrame + framesPerUpdates < customEndFrame) {
                                currentFrame += framesPerUpdates;
                                nextFrameIsLast = false;
                            } else {
                                nextFrameIsLast = true;
                            }
                        }
                    } else {
                        if (currentFrame + framesPerUpdates < (customEndFrame > 0 ? customEndFrame : metaData[0])) {
                            if (autoRepeat == 3) {
                                nextFrameIsLast = true;
                                autoRepeatPlayCount++;
                            } else {
                                currentFrame += framesPerUpdates;
                                nextFrameIsLast = false;
                            }
                        } else if (autoRepeat == 1) {
                            currentFrame = 0;
                            nextFrameIsLast = false;
                        } else if (autoRepeat == 2) {
                            currentFrame = 0;
                            nextFrameIsLast = true;
                            autoRepeatPlayCount++;
                        } else {
                            nextFrameIsLast = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            uiHandler.post(uiRunnable);
            if (frameWaitSync != null) {
                frameWaitSync.countDown();
            }
        }
    };


    private Builder builder;

    public AXrLottieDrawable(Builder builder) {
        this.builder = builder;
        if (builder.loaderListener != null)
            setOnLottieLoaderListener(loaderListener);

        switch (builder.type) {
            case FILE:
                initFromFile(builder.file, builder.cacheName, builder.w, builder.h, builder.cache, builder.limitFps, builder.colorReplacement);
                break;
            case JSON:
                initFromJson(builder.json, builder.cacheName, builder.w, builder.h, builder.cache, builder.limitFps, builder.startDecode, builder.colorReplacement);
                break;
            case URL:
                this.width = builder.w;
                this.height = builder.h;
                AXrLottieNetworkFetcher.load(AXrLottie.context, builder.url, this, builder.fetcher);
                break;
        }

        if (builder.customEndFrame > 0)
            setCustomEndFrame(builder.customEndFrame);

        if (builder.autoRepeat > 0)
            setAutoRepeat(builder.autoRepeat);

        if (builder.properties != null) {
            if (newPropertyUpdates == null) newPropertyUpdates = new ArrayList<>();
            newPropertyUpdates.addAll(builder.properties);
        }

        if (builder.listener != null)
            setOnFrameChangedListener(builder.listener);
        if (builder.render != null)
            setOnFrameRenderListener(render);

        if (builder.autoStart)
            start();
    }

    private void initFromJson(String json, String name, int width, int height, boolean cache, boolean limitFps, boolean startDecode, int[] colorReplacement) {
        if (cache) {
            File f = CacheWriter.load(json, name);
            if (f != null) {
                initFromFile(f, name, width, height, true, limitFps, colorReplacement);
                return;
            }
        }

        this.width = width;
        this.height = height;
        autoRepeat = 0;
        shouldLimitFps = limitFps;
        this.cacheName = name;
        getPaint().setFlags(Paint.FILTER_BITMAP_FLAG);
        nativePtr = createWithJson(json, name, metaData, colorReplacement);
        timeBetweenFrames = Math.max(shouldLimitFps ? 33 : 16, (int) (1000.0f / metaData[1]));
        if (startDecode) {
            setAllowDecodeSingleFrame(true);
        }
        lottieLoaded();
    }

    private void initFromFile(File file, String name, int width, int height, boolean precache, boolean limitFps, int[] colorReplacement) {
        this.width = width;
        this.height = height;
        shouldLimitFps = limitFps;
        this.cacheName = name;
        getPaint().setFlags(Paint.FILTER_BITMAP_FLAG);

        nativePtr = create(file.getAbsolutePath(), width, height, metaData, precache, colorReplacement, shouldLimitFps);
        if (precache && lottieCacheGenerateQueue == null) {
            lottieCacheGenerateQueue = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        }
        if (shouldLimitFps && metaData[1] < 60) {
            shouldLimitFps = false;
        }
        timeBetweenFrames = Math.max(shouldLimitFps ? 33 : 16, (int) (1000.0f / metaData[1]));
        lottieLoaded();
    }

    public AXrLottieDrawable newDrawable() {
        return builder.build();
    }

    public Builder getBuilder() {
        return builder;
    }

    public boolean hasLoaded() {
        return nativePtr != 0;
    }

    private String cacheName;

    /**
     * @return animation name (cacheName) (or animation file AbsolutePath)
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Returns default framerate of the Lottie resource.
     *
     * @return framerate of the Lottie resource
     */
    public int getTotalFrame() {
        return metaData[0];
    }

    /**
     * Returns total number of frames present in the Lottie resource.
     *
     * <i>Note: frame number starts with 0.</i>
     *
     * @return frame count of the Lottie resource.
     */
    public int getFrameRate() {
        return metaData[1];
    }

    public boolean isLoadingFromCache() {
        return metaData[2] == 1;
    }

    /**
     * @return current frame index
     */
    public int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Returns total animation duration of Lottie resource in second.
     * it uses totalFrame() and frameRate() to calculate the duration.
     * duration = totalFrame() / frameRate().
     *
     * @return total animation duration in second. or 0 if the Lottie resource has no animation.
     * @see #getTotalFrame()
     * @see #getFrameRate()
     */
    public long getDuration() {
        return (long) (metaData[0] / (float) metaData[1] * 1000);
    }

    public void setPlayInDirectionOfCustomEndFrame(boolean value) {
        playInDirectionOfCustomEndFrame = value;
    }

    public void setCustomEndFrame(int frame) {
        if (customEndFrame > metaData[0]) {
            return;
        }
        customEndFrame = frame;
    }

    private void invalidateInternal() {
        if (getCallback() != null) {
            invalidateSelf();
        }
    }

    public void setAllowDecodeSingleFrame(boolean value) {
        decodeSingleFrame = value;
        if (decodeSingleFrame) {
            scheduleNextGetFrame();
        }
    }

    public void recycle() {
        isRunning = false;
        isRecycled = true;
        checkRunningTasks();

        if (loadFrameTask == null && cacheGenerateTask == null) {
            if (nativePtr != 0) {
                destroy(nativePtr);
                nativePtr = 0;
            }
            if (secondNativePtr != 0) {
                destroy(secondNativePtr);
                secondNativePtr = 0;
            }
            recycleResources();
        } else {
            destroyWhenDone = true;
        }
    }

    public void setAutoRepeat(int value) {
        if (autoRepeat == 2 && value == 3 && currentFrame != 0) {
            return;
        }
        autoRepeat = value;
    }

    public void setAutoRepeat(boolean enabled) {
        setAutoRepeat(enabled ? 1 : 0);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            recycle();
        } finally {
            super.finalize();
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void start() {
        if (isRunning || autoRepeat >= 2 && autoRepeatPlayCount != 0) {
            return;
        }
        isRunning = true;
        if (invalidateOnProgressSet) {
            isInvalid = true;
            if (loadFrameTask != null) {
                doNotRemoveInvalidOnFrameReady = true;
            }
        }
        scheduleNextGetFrame();
        invalidateInternal();
    }

    public boolean restart() {
        if (autoRepeat < 2 || autoRepeatPlayCount == 0) {
            return false;
        }
        autoRepeatPlayCount = 0;
        autoRepeat = 2;
        start();
        return true;
    }

    public void beginApplyLayerProperties() {
        applyingLayerColors = true;
    }

    public void commitApplyLayerProperties() {
        if (!applyingLayerColors) {
            return;
        }
        applyingLayerColors = false;
        if (!isRunning && decodeSingleFrame) {
            if (currentFrame <= 2) {
                currentFrame = 0;
            }
            nextFrameIsLast = false;
            singleFrameDecoded = false;
            if (!scheduleNextGetFrame()) {
                forceFrameRedraw = true;
            }
        }
        invalidateInternal();
    }

    public void replaceColors(int[] colors) {
        newReplaceColors = colors;
        requestRedraw();
    }

    /**
     * Sets property value for the specified layer. layer can resolve
     * to multiple contents. In that case, the callback's value will apply to all of them.
     * <p>
     * layer should contain object names separated by (.) and can handle globe(**) or wildchar(*).
     */
    public void setLayerProperty(String layerName, AXrLottieProperty property) {
        newPropertyUpdates.add(new AXrLottieProperty.PropertyUpdate(property, layerName));
        requestRedraw();
    }

    void setLayerProperties(List<AXrLottieProperty.PropertyUpdate> list) {
        newPropertyUpdates.addAll(list);
        requestRedraw();
    }

    private void requestRedraw() {
        if (!applyingLayerColors && !isRunning && decodeSingleFrame) {
            if (currentFrame <= 2) {
                currentFrame = 0;
            }
            nextFrameIsLast = false;
            singleFrameDecoded = false;
            if (!scheduleNextGetFrame()) {
                forceFrameRedraw = true;
            }
        }
        invalidateInternal();
    }

    private boolean scheduleNextGetFrame() {
        if (!hasLoaded()) return false;
        if (loadFrameTask != null || nextRenderingBitmap != null || nativePtr == 0 || destroyWhenDone || !isRunning && (!decodeSingleFrame || decodeSingleFrame && singleFrameDecoded)) {
            return false;
        }
        if (!newPropertyUpdates.isEmpty()) {
            pendingPropertyUpdates.addAll(newPropertyUpdates);
            newPropertyUpdates.clear();
        }
        if (newReplaceColors != null) {
            pendingReplaceColors = newReplaceColors;
            newReplaceColors = null;
        }
        loadFrameRunnableQueue.execute(loadFrameTask = loadFrameRunnable);
        return true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    public void setCurrentFrame(int frame) {
        setCurrentFrame(frame, true);
    }

    public void setCurrentFrame(int frame, boolean async) {
        setCurrentFrame(frame, true, false);
    }

    public void setCurrentFrame(int frame, boolean async, boolean resetFrame) {
        if (frame < 0 || frame > metaData[0]) {
            return;
        }
        currentFrame = frame;
        nextFrameIsLast = false;
        singleFrameDecoded = false;
        if (invalidateOnProgressSet) {
            isInvalid = true;
            if (loadFrameTask != null) {
                doNotRemoveInvalidOnFrameReady = true;
            }
        }
        if ((!async || resetFrame) && waitingForNextTask && nextRenderingBitmap != null) {
            backgroundBitmap = nextRenderingBitmap;
            nextRenderingBitmap = null;
            loadFrameTask = null;
            waitingForNextTask = false;
        }
        if (!async) {
            if (loadFrameTask == null) {
                frameWaitSync = new CountDownLatch(1);
            }
        }
        if (scheduleNextGetFrame()) {
            if (!async) {
                try {
                    frameWaitSync.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                frameWaitSync = null;
            }
        } else {
            forceFrameRedraw = true;
        }
        invalidateSelf();
    }

    public void setProgressMs(long ms) {
        int frameNum = (int) ((Math.max(0, ms) / timeBetweenFrames) % metaData[0]);
        setCurrentFrame(frameNum, true, true);
    }

    public void setProgress(float progress) {
        setProgress(progress, true);
    }

    public void setProgress(float progress, boolean async) {
        if (progress < 0.0f) {
            progress = 0.0f;
        } else if (progress > 1.0f) {
            progress = 1.0f;
        }
        setCurrentFrame((int) (metaData[0] * progress), async);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        applyTransformation = true;
    }

    private void setCurrentFrame(long now, long timeDiff, long timeCheck, boolean force) {
        backgroundBitmap = renderingBitmap;
        renderingBitmap = nextRenderingBitmap;
        nextRenderingBitmap = null;
        if (render != null) render.onUpdate(this, currentFrame, timeDiff, force);
        if (nextFrameIsLast) {
            stop();
        }
        loadFrameTask = null;
        if (doNotRemoveInvalidOnFrameReady) {
            doNotRemoveInvalidOnFrameReady = false;
        } else if (isInvalid) {
            isInvalid = false;
        }
        singleFrameDecoded = true;
        waitingForNextTask = false;
        if (AXrLottie.getScreenRefreshRate() <= 60) {
            lastFrameTime = now;
        } else {
            lastFrameTime = now - Math.min(16, timeDiff - timeCheck);
        }
        if (force && forceFrameRedraw) {
            singleFrameDecoded = false;
            forceFrameRedraw = false;
        }
        if (listener != null) listener.onFrameChanged(this, currentFrame);
        scheduleNextGetFrame();
    }

    @Override
    public void draw(Canvas canvas) {
        if (nativePtr == 0 || destroyWhenDone) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        long timeDiff = Math.abs(now - lastFrameTime);
        int timeCheck;
        if (AXrLottie.getScreenRefreshRate() <= 60) {
            timeCheck = timeBetweenFrames - 6;
        } else {
            timeCheck = timeBetweenFrames;
        }
        if (isRunning) {
            if (renderingBitmap == null && nextRenderingBitmap == null) {
                scheduleNextGetFrame();
            } else if (nextRenderingBitmap != null && (renderingBitmap == null || timeDiff >= timeCheck)) {
                //update
                setCurrentFrame(now, timeDiff, timeCheck, false);
            }
        } else if ((forceFrameRedraw || decodeSingleFrame && timeDiff >= timeCheck) && nextRenderingBitmap != null) {
            setCurrentFrame(now, timeDiff, timeCheck, true);
        }

        if (!isInvalid && renderingBitmap != null) {
            if (applyTransformation) {
                dstRect.set(getBounds());
                scaleX = (float) dstRect.width() / width;
                scaleY = (float) dstRect.height() / height;
                applyTransformation = false;
            }
            canvas.save();
            canvas.translate(dstRect.left, dstRect.top);
            canvas.scale(scaleX, scaleY);

            canvas.drawBitmap(render(renderingBitmap, currentFrame), 0, 0, getPaint());
            if (isRunning) {
                invalidateInternal();
            }
            canvas.restore();
        }
    }

    @Override
    public int getMinimumHeight() {
        return height;
    }

    @Override
    public int getMinimumWidth() {
        return width;
    }

    public Bitmap getRenderingBitmap() {
        return renderingBitmap;
    }

    public Bitmap getNextRenderingBitmap() {
        return nextRenderingBitmap;
    }

    public Bitmap getBackgroundBitmap() {
        return backgroundBitmap;
    }

    public long getNativePtr() {
        return nativePtr;
    }

    /**
     * @return frame bitmap
     */
    public Bitmap getAnimatedBitmap() {
        if (renderingBitmap != null) {
            return renderingBitmap;
        } else if (nextRenderingBitmap != null) {
            return nextRenderingBitmap;
        }
        return null;
    }

    public boolean hasBitmap() {
        return nativePtr != 0 && (renderingBitmap != null || nextRenderingBitmap != null) && !isInvalid;
    }

    public void setInvalidateOnProgressSet(boolean value) {
        invalidateOnProgressSet = value;
    }

    public AXrLottieFrame getCurrentLottieFrame() {
        AXrLottieFrame frame = new AXrLottieFrame();
        frame.bitmap = getRenderingBitmap();
        frame.frame = currentFrame;
        frame.loaded = frame.bitmap != null;
        return frame;
    }

    /**
     * @return frame info at the giving frame index
     */
    public AXrLottieFrame getLottieFrameAt(int frame) {
        return getLottieFrameAt(frame, width, height);
    }

    /**
     * @return frame info at the giving frame index
     */
    public AXrLottieFrame getLottieFrameAt(int frame, int width, int height) {
        Bitmap backgroundBitmap = null;
        AXrLottieFrame cframe = new AXrLottieFrame();
        try {
            backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (backgroundBitmap != null) {
            try {
                if (!pendingPropertyUpdates.isEmpty()) {
                    for (AXrLottieProperty.PropertyUpdate entry : pendingPropertyUpdates) {
                        entry.apply(nativePtr);
                    }
                    pendingPropertyUpdates.clear();
                }
            } catch (Exception ignore) {

            }
            if (pendingReplaceColors != null) {
                AXrLottieNative.replaceColors(nativePtr, pendingReplaceColors);
                pendingReplaceColors = null;
            }

            int result = getFrame(nativePtr, frame, backgroundBitmap, width, height, backgroundBitmap.getRowBytes());
            cframe.loaded = (result != -1);
        }
        cframe.bitmap = backgroundBitmap;
        cframe.frame = frame;
        return cframe;
    }

    /**
     * @return Size of layers of the composition.
     */
    public int getLayersCount() {
        return AXrLottieNative.getLayersCount(nativePtr);
    }

    /**
     * Returns Layer information {name, inFrame, outFrame} of an specific layer of the composition.
     *
     * @return Layer Information of the Composition.
     * @see AXrLottieLayerInfo
     */
    public AXrLottieLayerInfo getLayerInfo(int index) {
        return new AXrLottieLayerInfo(AXrLottieNative.getLayerData(nativePtr, index));
    }

    /**
     * Returns Layer information {name, inFrame, outFrame} of an specific layer of the composition.
     * or null if layer doesn't exists
     *
     * @return Layer Information of the Composition.
     * @see AXrLottieLayerInfo
     */
    public AXrLottieLayerInfo getLayerInfo(String name) {
        int max = getLayersCount();
        for (int i = 0; i < max; i++) {
            AXrLottieLayerInfo info = getLayerInfo(i);
            if (info.getName().equals(name)) return info;
        }
        return null;
    }

    /**
     * Returns Layer information {name, inFrame, outFrame} of all the child layers  of the composition.
     *
     * @return List of Layer Information of the Composition.
     * @see AXrLottieLayerInfo
     */
    public List<AXrLottieLayerInfo> getLayers() {
        List<AXrLottieLayerInfo> layers = new ArrayList<>();
        int max = getLayersCount();
        for (int i = 0; i < max; i++) {
            layers.add(getLayerInfo(i));
        }
        return layers;
    }

    protected Bitmap render(Bitmap bitmap, int frame) {
        Bitmap r = null;
        if (render != null) r = render.renderFrame(this, bitmap, frame);
        if (r == null) r = bitmap;
        return r;
    }

    /**
     * lottie will call this when animation loaded
     */
    protected void lottieLoaded() {
        if (loaderListener != null) loaderListener.onLoaded(this);
    }

    public void load(File file) {
        initFromFile(file, builder.cacheName, builder.w, builder.h, builder.cache, builder.limitFps, builder.colorReplacement);
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateInternal();
                if (isRunning) start();
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof AXrLottieDrawable)) return false;

        AXrLottieDrawable that = (AXrLottieDrawable) o;

        if (width != that.width) return false;
        if (height != that.height) return false;
        if (customEndFrame != that.customEndFrame) return false;
        if (autoRepeat != that.autoRepeat) return false;
        return cacheName.equals(that.cacheName);
    }


    private static ThreadLocal<byte[]> readBufferLocal = new ThreadLocal<>();
    private static ThreadLocal<byte[]> bufferLocal = new ThreadLocal<>();

    private static String readRes(Context context, Object asset, int rawRes) {
        InputStream inputStream = null;
        try {
            if (asset != null) {
                inputStream = context.getAssets().open(String.valueOf(asset));
            } else {
                inputStream = context.getResources().openRawResource(rawRes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readStream(inputStream);
    }

    private static String readStream(InputStream inputStream) {
        if (inputStream == null) return null;
        int totalRead = 0;
        byte[] readBuffer = readBufferLocal.get();
        if (readBuffer == null) {
            readBuffer = new byte[64 * 1024];
            readBufferLocal.set(readBuffer);
        }
        try {
            int readLen;
            byte[] buffer = bufferLocal.get();
            if (buffer == null) {
                buffer = new byte[4096];
                bufferLocal.set(buffer);
            }
            while ((readLen = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                if (readBuffer.length < totalRead + readLen) {
                    byte[] newBuffer = new byte[readBuffer.length * 2];
                    System.arraycopy(readBuffer, 0, newBuffer, 0, totalRead);
                    readBuffer = newBuffer;
                    readBufferLocal.set(readBuffer);
                }
                if (readLen > 0) {
                    System.arraycopy(buffer, 0, readBuffer, totalRead, readLen);
                    totalRead += readLen;
                }
            }
        } catch (Throwable e) {
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable ignore) {
            }
        }

        return new String(readBuffer, 0, totalRead);
    }

    public static Builder fromPath(String path) {
        return new Builder(new File(path));
    }

    public static Builder fromFile(File file) {
        return new Builder(file);
    }

    public static Builder fromURL(String url) {
        return new Builder(url, new AXrLottieSimpleNetworkFetcher());
    }

    public static Builder fromURL(String url, AXrLottieNetworkFetcher networkFetcher) {
        return new Builder(url, networkFetcher);
    }

    public static Builder fromJson(String JSON, String cacheName) {
        return new Builder(JSON, cacheName);
    }

    public static Builder fromAssets(@NonNull Context context, String fileName) {
        return new Builder(readRes(context, fileName, 0), fileName.replaceAll("\\W+", ""));
    }

    public static Builder fromRes(@NonNull Context context, int res, String cacheName) {
        return new Builder(readRes(context, null, res), cacheName);
    }

    public static Builder fromInputStream(@NonNull InputStream inputStream, String cacheName) {
        return new Builder(readStream(inputStream), cacheName);
    }

    private enum BuilderType {
        JSON, FILE, URL
    }

    public static class Builder {
        private final BuilderType type;
        private final File file;
        private final String json;
        private final String url;
        private final AXrLottieNetworkFetcher fetcher;
        private String cacheName;
        private int w = 200, h = 200;
        private boolean cache = true;
        private boolean limitFps = false;
        private boolean startDecode = true;
        private int[] colorReplacement = null;
        private List<AXrLottieProperty.PropertyUpdate> properties = null;
        private int customEndFrame = -1;
        private int autoRepeat = 0;
        private OnFrameChangedListener listener = null;
        private OnFrameRenderListener render = null;
        private OnLottieLoaderListener loaderListener = null;
        private boolean autoStart = false;

        public Builder(File file) {
            if (file == null) {
                throw new NullPointerException("lottie file can't be null!");
            }
            this.file = file;
            this.cacheName = file.getAbsolutePath();
            this.json = null;
            this.url = null;
            this.fetcher = null;
            this.type = BuilderType.FILE;
        }

        public Builder(String json, String cacheName) {
            this.file = null;
            this.json = json;
            this.url = null;
            this.type = BuilderType.JSON;
            this.fetcher = null;
            setCacheName(cacheName);
        }

        public Builder(String url, AXrLottieNetworkFetcher fetcher) {
            if (fetcher == null) {
                throw new NullPointerException("NetworkFetcher can't be null!");
            }
            this.file = null;
            this.json = null;
            this.url = "lottie_cache_" + url.replaceAll("\\W+", "");
            this.type = BuilderType.URL;
            this.fetcher = fetcher;
        }

        /**
         * will be used to cache the JSON string data and compare drawables.
         */
        public Builder setCacheName(String cacheName) {
            if (cacheName == null || cacheName.isEmpty()) {
                throw new NullPointerException("lottie name (cacheName) can not be null!");
            }
            this.cacheName = cacheName;
            return this;
        }

        /**
         * set lottie min width and height
         */
        public Builder setSize(int w, int h) {
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
        public Builder setCacheEnabled(boolean enabled) {
            this.cache = enabled;
            return this;
        }

        /**
         * set lottie frame rate limit
         */
        public Builder setFpsLimit(boolean limitFps) {
            this.limitFps = limitFps;
            return this;
        }

        public Builder setColorReplacement(int[] colorReplacement) {
            this.colorReplacement = colorReplacement;
            return this;
        }

        public Builder setAllowDecodeSingleFrame(boolean startDecode) {
            this.startDecode = startDecode;
            return this;
        }

        /**
         * Sets property value for the specified layer. layer can resolve
         * to multiple contents. In that case, the callback's value will apply to all of them.
         * <p>
         * layer should contain object names separated by (.) and can handle globe(**) or wildchar(*).
         */
        public Builder addLayerProperty(String layerName, AXrLottieProperty property) {
            if (properties == null) properties = new ArrayList<>();
            properties.add(new AXrLottieProperty.PropertyUpdate(property, layerName));
            return this;
        }

        public Builder setCustomEndFrame(int customEndFrame) {
            this.customEndFrame = customEndFrame;
            return this;
        }

        public Builder setAutoRepeat(int count) {
            this.autoRepeat = count;
            return this;
        }

        public Builder setAutoRepeat(boolean enabled) {
            this.autoRepeat = enabled ? 1 : 0;
            return this;
        }

        public Builder setAutoStart(boolean autoStart) {
            this.autoStart = autoStart;
            return this;
        }

        public Builder setOnFrameChangedListener(OnFrameChangedListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setOnFrameRenderListener(OnFrameRenderListener render) {
            this.render = render;
            return this;
        }

        public Builder setOnLottieLoaderListener(OnLottieLoaderListener loaderListener) {
            this.loaderListener = loaderListener;
            return this;
        }

        public AXrLottieDrawable build() {
            return new AXrLottieDrawable(this);
        }
    }

}
