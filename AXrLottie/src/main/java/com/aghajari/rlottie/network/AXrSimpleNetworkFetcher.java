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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AXrSimpleNetworkFetcher is the default network fetching stack built on HttpURLConnection.
 */
public class AXrSimpleNetworkFetcher extends AXrLottieNetworkFetcher {

    private static final String TAG = AXrSimpleNetworkFetcher.class.getSimpleName();

    @NonNull
    @Override
    public AXrLottieFetchResult fetchSync(@NonNull String url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(getConnectTimeout());
        connection.setReadTimeout(getReadTimeout());
        connection.connect();
        return new AXrSimpleLottieFetchResult(connection);
    }

    static class AXrSimpleLottieFetchResult implements AXrLottieFetchResult {

        @NonNull
        private final HttpURLConnection connection;

        AXrSimpleLottieFetchResult(@NonNull HttpURLConnection connection) {
            this.connection = connection;
        }

        @Override
        public boolean isSuccessful() {
            try {
                return connection.getErrorStream() == null && connection.getResponseCode() / 100 == 2;
            } catch (IOException e) {
                return false;
            }
        }

        @NonNull
        @Override
        public InputStream bodyByteStream() throws IOException {
            return connection.getInputStream();
        }

        @Nullable
        @Override
        public String contentType() {
            return connection.getContentType();
        }

        @Nullable
        @Override
        public String error() {
            try {
                return isSuccessful() ? null :
                        "Unable to fetch " + connection.getURL() + ". Failed with " + connection.getResponseCode() + "\n" + getErrorFromConnection(connection);
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        public void close() {
            connection.disconnect();
        }

        private String getErrorFromConnection(HttpURLConnection connection) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder error = new StringBuilder();
            String line;

            try {
                while ((line = r.readLine()) != null) {
                    error.append(line).append('\n');
                }
            } catch (Exception e) {
                Log.e(TAG, "getErrorFromConnection: ", e);
            } finally {
                try {
                    r.close();
                } catch (Exception ignore) {
                }
            }
            return error.toString();
        }
    }
}
