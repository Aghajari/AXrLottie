package com.aghajari.rlottie.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * The result of the operation of obtaining a Lottie animation
 */
public interface AXrLottieFetchResult extends Closeable {
    /**
     * @return Is the operation successful
     */
    boolean isSuccessful();

    /**
     * @return Received content stream
     */
    @NonNull
    InputStream bodyByteStream() throws IOException;

    /**
     * @return Type of content received
     */
    @Nullable
    String contentType();

    /**
     * @return Operation error
     */
    @Nullable
    String error();
}