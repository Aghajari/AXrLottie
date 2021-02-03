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


package com.aghajari.rlottie.network;


import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * AXrLottie has a default network fetching {@link AXrSimpleNetworkFetcher} stack built on {@link java.net.HttpURLConnection}.
 * However, if you would like to hook into your own network stack for performance,
 * caching, or analytics, you may replace the internal stack with your own.
 */
public abstract class AXrLottieNetworkFetcher {

    public abstract AXrLottieFetchResult fetchSync(@NonNull String url) throws IOException;

    private int connectTimeout = 10_000;
    private int readTimeout = 10_000;

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        updateClient();
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        updateClient();
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    protected void updateClient() {
    }

}
