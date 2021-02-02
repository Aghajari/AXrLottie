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

import android.content.Context;

import com.aghajari.rlottie.AXrLottie;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SimpleNetworkFetcher extends AXrLottieNetworkFetcher {

    private SimpleNetworkFetcher(){}

    public static SimpleNetworkFetcher create(){
        return new SimpleNetworkFetcher();
    }

    @Override
    protected void fetchFromNetwork(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try {
                    connection = (HttpURLConnection) new URL(getURL()).openConnection();
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(AXrLottie.getNetworkTimeOut());
                    connection.setReadTimeout(AXrLottie.getNetworkTimeOut());

                    try {
                        connection.connect();
                        if (connection.getErrorStream() != null || connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            onError(connection.getErrorStream(), connection.getResponseCode());
                            return;
                        }

                        parseStream(context,connection.getInputStream(),connection.getContentType());
                    } catch (Exception e) {
                        onError(e);
                    } finally {
                        connection.disconnect();
                    }
                } catch (MalformedURLException e1) {
                    onError(e1);
                } catch (IOException e) {
                    onError(e);
                }
            }
        });
        thread.start();
    }

    public void onError(InputStream errorInputStream, int responseCode) {
        onError(new Exception("code=" + responseCode));
    }

}
