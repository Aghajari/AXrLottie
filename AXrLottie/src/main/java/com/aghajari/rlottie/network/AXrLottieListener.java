package com.aghajari.rlottie.network;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * Receive a result with either the value or exception for a {@link AXrLottieTask}
 */
public interface AXrLottieListener<T> {
    void onResult(T result);
}
