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


package com.aghajari.rlottie.network;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.aghajari.rlottie.decoder.AXrLottieResult;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * Helper to run asynchronous tasks with a result.
 * Results can be obtained with {@link #addListener(Listener)}.
 * Failures can be obtained with {@link #addFailureListener(Listener)}.
 * <p>
 * A task will produce a single result or a single failure.
 */
public class AXrLottieTask<T> {

    /**
     * Set this to change the executor that LottieTasks are run on. This will be the executor that composition parsing and url
     * fetching happens on.
     * <p>
     * You may change this to run deserialization synchronously for testing.
     */
    @SuppressWarnings("WeakerAccess")
    public static Executor EXECUTOR = Executors.newCachedThreadPool();

    /* Preserve add order. */
    private final Set<Listener<T>> successListeners = new LinkedHashSet<>(1);
    private final Set<Listener<Throwable>> failureListeners = new LinkedHashSet<>(1);
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    private volatile AXrLottieResult<T> result = null;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public AXrLottieTask(Callable<AXrLottieResult<T>> runnable) {
        this(runnable, false);
    }

    /**
     * runNow is only used for testing.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    AXrLottieTask(Callable<AXrLottieResult<T>> runnable, boolean runNow) {
        if (runNow) {
            try {
                setResult(runnable.call());
            } catch (Throwable e) {
                setResult(new AXrLottieResult<>(e));
            }
        } else {
            EXECUTOR.execute(new LottieFutureTask(runnable));
        }
    }

    private void setResult(@Nullable AXrLottieResult<T> result) {
        if (this.result != null) {
            throw new IllegalStateException("A task may only be set once.");
        }
        this.result = result;
        notifyListeners();
    }

    /**
     * Add a task listener. If the task has completed, the listener will be called synchronously.
     *
     * @return the task for call chaining.
     */
    public synchronized AXrLottieTask<T> addListener(Listener<T> listener) {
        if (result != null && result.getValue() != null) {
            listener.onResult(result.getValue());
        }

        successListeners.add(listener);
        return this;
    }

    /**
     * Remove a given task listener. The task will continue to execute so you can re-add
     * a listener if necessary.
     *
     * @return the task for call chaining.
     */
    public synchronized AXrLottieTask<T> removeListener(Listener<T> listener) {
        successListeners.remove(listener);
        return this;
    }

    /**
     * Add a task failure listener. This will only be called in the even that an exception
     * occurs. If an exception has already occurred, the listener will be called immediately.
     *
     * @return the task for call chaining.
     */
    public synchronized AXrLottieTask<T> addFailureListener(Listener<Throwable> listener) {
        if (result != null) {
            Throwable throwable = result.getException();
            if (throwable != null) {
                listener.onResult(throwable);
            }
        }

        failureListeners.add(listener);
        return this;
    }

    /**
     * Remove a given task failure listener. The task will continue to execute so you can re-add
     * a listener if necessary.
     *
     * @return the task for call chaining.
     */
    public synchronized AXrLottieTask<T> removeFailureListener(Listener<Throwable> listener) {
        failureListeners.remove(listener);
        return this;
    }

    private void notifyListeners() {
        // Listeners should be called on the main thread.
        handler.post(() -> {
            // Local reference in case it gets set on a background thread.
            AXrLottieResult<T> result = AXrLottieTask.this.result;
            if (result == null)
                return;

            if (result.getValue() != null) {
                notifySuccessListeners(result.getValue());
            } else if (result.getException() != null) {
                notifyFailureListeners(result.getException());
            }
        });
    }

    private synchronized void notifySuccessListeners(T value) {
        // Allows listeners to remove themselves in onResult.
        // Otherwise we risk ConcurrentModificationException.
        List<Listener<T>> listenersCopy = new ArrayList<>(successListeners);
        for (Listener<T> l : listenersCopy) {
            l.onResult(value);
        }
    }

    private synchronized void notifyFailureListeners(Throwable e) {
        // Allows listeners to remove themselves in onResult.
        // Otherwise we risk ConcurrentModificationException.
        List<Listener<Throwable>> listenersCopy = new ArrayList<>(failureListeners);
        if (listenersCopy.isEmpty()) {
            return;
        }

        for (Listener<Throwable> l : listenersCopy) {
            l.onResult(e);
        }
    }

    private class LottieFutureTask extends FutureTask<AXrLottieResult<T>> {
        LottieFutureTask(Callable<AXrLottieResult<T>> callable) {
            super(callable);
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                // We don't need to notify and listeners if the task is cancelled.
                return;
            }

            try {
                setResult(get());
            } catch (InterruptedException | ExecutionException e) {
                setResult(new AXrLottieResult<>(e));
            }
        }
    }

    /**
     * Receive a result with either the value or exception for a {@link AXrLottieTask}
     */
    public interface Listener<T> {
        void onResult(T result);
    }
}