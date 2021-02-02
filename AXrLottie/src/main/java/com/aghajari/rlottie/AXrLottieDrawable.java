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
import androidx.annotation.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.aghajari.rlottie.network.AXrLottieNetworkFetcher;
import com.aghajari.rlottie.network.SimpleNetworkFetcher;

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
    private float speed = 1f;

    private int customStartFrame = -1;
    private int customEndFrame = -1;
    private boolean playInDirectionOfCustomEndFrame = false;
    private AXrLottieMarker selectedMarker = null;

    private ArrayList<AXrLottieProperty.PropertyUpdate> newPropertyUpdates = new ArrayList<>();
    private volatile ArrayList<AXrLottieProperty.PropertyUpdate> pendingPropertyUpdates = new ArrayList<>();

    public final static int AUTO_REPEAT_INFINITE = -1;
    private int autoRepeat = AUTO_REPEAT_INFINITE;
    private int autoRepeatPlayCount;

    public final static int REPEAT_MODE_RESTART = 1;
    public final static int REPEAT_MODE_REVERSE = 2;
    private int repeatMode = REPEAT_MODE_RESTART;
    private boolean repeatChangeDirection = false;

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

    protected float scaleX = 1.0f;
    protected float scaleY = 1.0f;
    private boolean applyTransformation;
    private final Rect dstRect = new Rect();
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private volatile boolean isRunning;
    private volatile boolean isRecycled;
    private volatile long nativePtr;

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
        void onRepeat (int repeatedCount,boolean lastFrame);
        void onStop();
        void onStart();
        void onRecycle();
    }

    public interface OnFrameRenderListener {
        void onUpdate(AXrLottieDrawable drawable, int frame, long timeDiff, boolean force);
        Bitmap renderFrame(AXrLottieDrawable drawable, Bitmap bitmap, int frame);
    }

    public interface OnLottieLoaderListener {
        void onLoaded(AXrLottieDrawable drawable);
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
            }
        }
        if (nativePtr == 0) {
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
                if (listener!=null) listener.onRecycle();
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
                    int framesPerUpdates = getFramesPerUpdate();
                    int endFrame = findEndFrame();
                    int startFrame = findStartFrame();

                    if (hasCustomEndFrame() && playInDirectionOfCustomEndFrame) {
                        if (currentFrame > endFrame) {
                            if (currentFrame - framesPerUpdates > endFrame) {
                                currentFrame -= framesPerUpdates;
                                nextFrameIsLast = false;
                            } else {
                                nextFrameIsLast = true;
                            }
                        } else {
                            if (currentFrame + framesPerUpdates < endFrame) {
                                currentFrame += framesPerUpdates;
                                nextFrameIsLast = false;
                            } else {
                                nextFrameIsLast = true;
                            }
                        }
                        if (currentFrame < startFrame){
                            currentFrame = startFrame;
                            nextFrameIsLast = true;
                        }
                    } else {
                        boolean seemsItsLastFrame = false;

                        if (repeatMode == REPEAT_MODE_REVERSE){
                            if (repeatChangeDirection) {
                                currentFrame -= framesPerUpdates;
                                if (currentFrame <= startFrame){
                                    repeatChangeDirection = false;
                                    seemsItsLastFrame = true;
                                }
                            } else {
                                currentFrame += framesPerUpdates;
                                if (currentFrame >= endFrame){
                                    repeatChangeDirection = true;
                                    seemsItsLastFrame = true;
                                }
                            }
                            nextFrameIsLast = false;
                        } else if (currentFrame + framesPerUpdates < endFrame) {
                            currentFrame += framesPerUpdates;
                            nextFrameIsLast = false;
                        } else if (autoRepeat == AUTO_REPEAT_INFINITE) {
                            currentFrame = startFrame;
                            nextFrameIsLast = false;

                            if (listener!=null)
                                listener.onRepeat(AUTO_REPEAT_INFINITE,false);
                        } else {
                            seemsItsLastFrame = true;
                        }

                        if (seemsItsLastFrame && autoRepeat >= 0){
                            autoRepeatPlayCount++;
                            if (autoRepeatPlayCount >= autoRepeat) {
                                repeatChangeDirection = false;
                                nextFrameIsLast = true;

                                if (listener!=null)
                                    listener.onRepeat(autoRepeatPlayCount,true);

                            } else if (repeatMode == REPEAT_MODE_RESTART) {
                                currentFrame = startFrame;

                                if (listener!=null)
                                    listener.onRepeat(autoRepeatPlayCount,false);
                            }
                        } else if (currentFrame > endFrame){
                            currentFrame = endFrame;
                        } else if (currentFrame < startFrame){
                            currentFrame = startFrame;
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

    protected int getFramesPerUpdate(){
        return shouldLimitFps ? 2 : 1;
    }

    protected boolean hasCustomEndFrame(){
        return customEndFrame > 0 || getSelectedMarker()!=null;
    }

    protected int findEndFrame(){
        if (getSelectedMarker() != null && getSelectedMarker().getOutFrame()>0)
            return Math.min(getSelectedMarker().getOutFrame(),metaData[0]);

        return customEndFrame > 0 ? customEndFrame : metaData[0];
    }

    protected int findStartFrame(){
        if (getSelectedMarker() != null && getSelectedMarker().getInFrame()>=0)
            return Math.min(getSelectedMarker().getInFrame(),metaData[0]);

        return Math.min(Math.max(customStartFrame, 0),metaData[0]);
    }

    protected int findTimeBetweenFrames(){
        return (int) (timeBetweenFrames / speed);
    }

    private Builder builder;

    public AXrLottieDrawable(Builder builder) {
        this.builder = builder;
        if (builder.loaderListener != null)
            setOnLottieLoaderListener(loaderListener);

        switch (builder.type) {
            case FILE:
                initFromFile(builder.file, builder.cacheName, builder.w, builder.h, builder.cache, builder.limitFps);
                break;
            case JSON:
                initFromJson(builder.json, builder.cacheName, builder.w, builder.h, builder.cache, builder.limitFps, builder.startDecode);
                break;
            case URL:
                this.width = builder.w;
                this.height = builder.h;
                cacheName = builder.cacheName;
                builder.fetcher.attachToDrawable(AXrLottie.context,this,builder.url,true);
                break;
        }

        if (builder.customEndFrame != DEFAULT)
            setCustomEndFrame(builder.customEndFrame);

        if (builder.customStartFrame != DEFAULT)
            setCustomStartFrame(builder.customStartFrame);

        if (builder.autoRepeat != DEFAULT)
            setAutoRepeat(builder.autoRepeat);

        if (builder.repeatMode != DEFAULT)
            setAutoRepeatMode(builder.repeatMode);

        if (builder.speed > 0)
            setSpeed(builder.speed);

        if (builder.properties != null) {
            if (newPropertyUpdates == null) newPropertyUpdates = new ArrayList<>();
            newPropertyUpdates.addAll(builder.properties);
        }

        if (builder.listener != null)
            setOnFrameChangedListener(builder.listener);

        if (builder.render != null)
            setOnFrameRenderListener(render);

        if (builder.selectedMarker != null)
            selectMarker(builder.selectedMarker);

        if (builder.autoStart)
            start();
    }

    private void initFromJson(String json, String name, int width, int height, boolean cache, boolean limitFps, boolean startDecode) {
        if (cache) {
            File f = CacheWriter.load(json, name);
            if (f != null) {
                initFromFile(f, name, width, height, true, limitFps);
                return;
            }
        }

        this.width = width;
        this.height = height;
        shouldLimitFps = limitFps;
        this.cacheName = name;
        getPaint().setFlags(Paint.FILTER_BITMAP_FLAG);
        nativePtr = createWithJson(json, name, metaData);
        timeBetweenFrames = Math.max(shouldLimitFps ? 33 : 16, (int) (1000.0f / metaData[1]));
        if (startDecode) {
            setAllowDecodeSingleFrame(true);
        }
        lottieLoaded();
    }

    private void initFromFile(File file, String name, int width, int height, boolean precache, boolean limitFps) {
        this.width = width;
        this.height = height;
        shouldLimitFps = limitFps;
        this.cacheName = name;
        getPaint().setFlags(Paint.FILTER_BITMAP_FLAG);

        nativePtr = create(file.getAbsolutePath(), width, height, metaData, precache, shouldLimitFps);
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

    public void setOnFrameChangedListener(OnFrameChangedListener listener) {
        this.listener = listener;
    }

    public void setOnFrameRenderListener(OnFrameRenderListener listener) {
        this.render = listener;
    }

    public void setOnLottieLoaderListener(OnLottieLoaderListener listener) {
        this.loaderListener = listener;
    }

    /**
     * Returns total number of frames present in the Lottie resource.
     *
     * @return count of the lottie resource frames.
     */
    public int getTotalFrame() {
        return metaData[0];
    }

    /**
     * Returns default framerate of the Lottie resource.
     *
     * @return framerate of the Lottie resource
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

    public void setCustomStartFrame(int frame) {
        if (customStartFrame > metaData[0]) {
            return;
        }
        customStartFrame = Math.max(frame,0);
    }

    public int getStartFrame(){
        return findStartFrame();
    }

    public int getEndFrame(){
        return findEndFrame();
    }

    public int getCustomStartFrame(){
        return customStartFrame;
    }

    public int getCustomEndFrame(){
        return customEndFrame;
    }

    /**
     * Select a custom marker
     *
     * @see AXrLottieMarker
     * @see AXrLottieDrawable#getMarkers()
     */
    public void selectMarker(@Nullable AXrLottieMarker marker) {
        this.selectedMarker = marker;
    }

    /**
     * @return selected marker
     * @see AXrLottieMarker
     * @see AXrLottieDrawable#getMarkers()
     */
    public @Nullable AXrLottieMarker getSelectedMarker() {
        return selectedMarker;
    }

    public void setSpeed(float speed) {
        if (speed<=0) return;
        this.speed = speed;
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
        if (isRunning && listener!=null) listener.onRecycle();

        isRunning = false;
        isRecycled = true;
        checkRunningTasks();

        if (loadFrameTask == null && cacheGenerateTask == null) {
            if (nativePtr != 0) {
                destroy(nativePtr);
                nativePtr = 0;
            }
            recycleResources();
        } else {
            destroyWhenDone = true;
        }
    }

    /**
     * Set auto repeat count
     *
     * @see AXrLottieDrawable#AUTO_REPEAT_INFINITE
     */
    public void setAutoRepeat(int repeatCount) {
        if (repeatCount>=0 && autoRepeatPlayCount>=repeatCount) return;
        if (repeatMode < AUTO_REPEAT_INFINITE) return;

        autoRepeat = repeatCount;
    }

    /**
     * Enable infinite auto repeat
     *
     * @see AXrLottieDrawable#setAutoRepeat(int)
     */
    public void setAutoRepeat(boolean enabled) {
        setAutoRepeat(enabled ? AUTO_REPEAT_INFINITE : 0);
    }

    /**
     * Set repeat mode
     *
     * @see AXrLottieDrawable#REPEAT_MODE_RESTART
     * @see AXrLottieDrawable#REPEAT_MODE_REVERSE
     */
    public void setAutoRepeatMode (int autoRepeatMode){
        if (autoRepeatMode != REPEAT_MODE_RESTART && autoRepeatMode != REPEAT_MODE_REVERSE)
            return; //ignore invalid modes

        this.repeatMode = autoRepeatMode;
        if (repeatMode != REPEAT_MODE_REVERSE)
            repeatChangeDirection = false;
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
        if (isRunning) return;
        if (listener!=null) listener.onStart();
        isRunning = true;
        repeatChangeDirection = false;
        if (invalidateOnProgressSet) {
            isInvalid = true;
            if (loadFrameTask != null) {
                doNotRemoveInvalidOnFrameReady = true;
            }
        }
        scheduleNextGetFrame();
        invalidateInternal();
    }

    public void restart() {
        autoRepeatPlayCount = 0;
        repeatChangeDirection = false;
        setCurrentFrame(findStartFrame(),true,true);
        if (isRunning){
            isRunning = false;
            start();
        }
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
            if (currentFrame <= findStartFrame()+2) {
                currentFrame = findStartFrame();
            }
            nextFrameIsLast = false;
            singleFrameDecoded = false;
            if (!scheduleNextGetFrame()) {
                forceFrameRedraw = true;
            }
        }
        invalidateInternal();
    }

    /**
     * Sets property value for the specified layer. layer can resolve
     * to multiple contents. In that case, the callback's value will apply to all of them.
     * <p>
     * keyPath should contain object names separated by (.) and can handle globe(**) or wildchar(*).
     */
    public void setLayerProperty(String keyPath, AXrLottieProperty property) {
        newPropertyUpdates.add(new AXrLottieProperty.PropertyUpdate(property, keyPath));
        requestRedraw();
    }

    void setLayerProperties(List<AXrLottieProperty.PropertyUpdate> list) {
        newPropertyUpdates.addAll(list);
        requestRedraw();
    }

    private void requestRedraw() {
        if (!applyingLayerColors && !isRunning && decodeSingleFrame) {
            if (currentFrame <= findStartFrame()+2) {
                currentFrame = findStartFrame();
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
        loadFrameRunnableQueue.execute(loadFrameTask = loadFrameRunnable);
        return true;
    }

    @Override
    public void stop() {
        isRunning = false;
        if (listener!=null) listener.onStop();
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
            timeCheck = (int) (findTimeBetweenFrames() - 6);
        } else {
            timeCheck = (int) findTimeBetweenFrames();
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

            int result = getFrame(nativePtr, frame, backgroundBitmap, width, height, backgroundBitmap.getRowBytes());
            cframe.loaded = (result != -1);
        }
        cframe.bitmap = backgroundBitmap;
        cframe.frame = frame;
        return cframe;
    }

    /**
     * @return composition layers count.
     */
    public int getLayersCount() {
        return AXrLottieNative.getLayersCount(nativePtr);
    }

    /**
     * Returns Layer information {name, inFrame, outFrame, type} of an specific layer of the composition.
     *
     * @return Layer Information of the Composition.
     * @see AXrLottieLayerInfo
     */
    public AXrLottieLayerInfo getLayerInfo(int index) {
        return new AXrLottieLayerInfo(AXrLottieNative.getLayerData(nativePtr, index));
    }

    /**
     * Returns Layer information {name, inFrame, outFrame, type} of an specific layer of the composition.
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
     * Returns Layer information {name, inFrame, outFrame, type} of all the child layers  of the composition.
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

    /**
     * @return composition markers count.
     */
    public int getMarkersCount() {
        return AXrLottieNative.getMarkersCount(nativePtr);
    }

    /**
     * @return Composition Marker
     * @see AXrLottieMarker
     */
    public AXrLottieMarker getMarker(int index) {
        return new AXrLottieMarker(AXrLottieNative.getMarkerData(nativePtr, index));
    }

    /**
     * @return Composition Marker
     * @see AXrLottieMarker
     */
    public AXrLottieMarker getMarker(String marker) {
        int max = getMarkersCount();
        for (int i = 0; i < max; i++) {
            AXrLottieMarker info = getMarker(i);
            if (info.getMarker().equals(marker)) return info;
        }
        return null;
    }

    /**
     * @return Composition Markers List
     * @see AXrLottieMarker
     */
    public List<AXrLottieMarker> getMarkers() {
        List<AXrLottieMarker> markers = new ArrayList<>();
        int max = getMarkersCount();
        for (int i = 0; i < max; i++) {
            markers.add(getMarker(i));
        }
        return markers;
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

    /**
     * called when animation loaded from {@link AXrLottieNetworkFetcher}
     */
    public void initFromNetwork(File file, AXrLottieNetworkFetcher fetcher) {
        if (hasLoaded() || !builder.cacheName.equalsIgnoreCase(fetcher.getCacheName())) return;

        initFromFile(file, builder.cacheName, builder.w, builder.h, builder.cache, builder.limitFps);
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    isRunning = false;
                    start();
                }else {
                    invalidateInternal();
                }
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AXrLottieDrawable)) return false;

        AXrLottieDrawable that = (AXrLottieDrawable) o;

        if (width != that.width) return false;
        if (height != that.height) return false;
        if (findEndFrame() != that.findEndFrame()) return false;
        if (findStartFrame() != that.findStartFrame()) return false;
        if (autoRepeat != that.autoRepeat) return false;
        if (repeatMode != that.repeatMode) return false;
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
        return new Builder(url, SimpleNetworkFetcher.create());
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

    private static final int DEFAULT = -100;

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
        private List<AXrLottieProperty.PropertyUpdate> properties = null;
        private int customEndFrame = DEFAULT;
        private int customStartFrame = DEFAULT;
        private int repeatMode = DEFAULT;
        private int autoRepeat = DEFAULT;
        private OnFrameChangedListener listener = null;
        private OnFrameRenderListener render = null;
        private OnLottieLoaderListener loaderListener = null;
        private boolean autoStart;
        private AXrLottieMarker selectedMarker = null;
        private float speed = -1;

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
            this.url = url;
            this.cacheName = "lottie_cache_" + url.replaceAll("\\W+", "");
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

        public Builder setSpeed(float speed) {
            this.speed = speed;
            return this;
        }

        /**
         * set lottie frame rate limit
         */
        public Builder setFpsLimit(boolean limitFps) {
            this.limitFps = limitFps;
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
         * keyPath should contain object names separated by (.) and can handle globe(**) or wildchar(*).
         */
        public Builder addLayerProperty(String keyPath, AXrLottieProperty property) {
            if (properties == null) properties = new ArrayList<>();
            properties.add(new AXrLottieProperty.PropertyUpdate(property, keyPath));
            return this;
        }

        public Builder setCustomEndFrame(int customEndFrame) {
            this.customEndFrame = customEndFrame;
            return this;
        }

        public Builder setCustomStartFrame(int customStartFrame) {
            this.customStartFrame = customStartFrame;
            return this;
        }

        public Builder setSelectedMarker(AXrLottieMarker marker) {
            this.selectedMarker = marker;
            return this;
        }

        /**
         * Set auto repeat count
         *
         * @see AXrLottieDrawable#AUTO_REPEAT_INFINITE
         */
        public Builder setAutoRepeat(int repeatCount) {
            autoRepeat = repeatCount;
            return this;
        }

        /**
         * Enable infinite auto repeat
         *
         * @see AXrLottieDrawable#setAutoRepeat(int)
         */
        public Builder setAutoRepeat(boolean enabled) {
            return setAutoRepeat(enabled ? AUTO_REPEAT_INFINITE : 0);
        }

        /**
         * Set repeat mode
         *
         * @see AXrLottieDrawable#REPEAT_MODE_RESTART
         * @see AXrLottieDrawable#REPEAT_MODE_REVERSE
         */
        public Builder setAutoRepeatMode (int autoRepeatMode){
            this.repeatMode = autoRepeatMode;
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
