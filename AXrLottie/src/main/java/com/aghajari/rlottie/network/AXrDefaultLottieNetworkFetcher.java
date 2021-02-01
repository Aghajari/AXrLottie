package com.aghajari.rlottie.network;

import androidx.annotation.NonNull;

import com.aghajari.rlottie.AXrL;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author kienht
 * @since 29/01/2021
 */
public class AXrDefaultLottieNetworkFetcher implements AXrLottieNetworkFetcher {

    private int connectTimeout = 10_000;
    private int readTimeout = 10_000;

    public AXrDefaultLottieNetworkFetcher() {
    }

    public AXrDefaultLottieNetworkFetcher(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @NonNull
    @Override
    public AXrLottieFetchResult fetchSync(@NonNull String url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.connect();
        return new AXrDefaultLottieFetchResult(connection);
    }
}
