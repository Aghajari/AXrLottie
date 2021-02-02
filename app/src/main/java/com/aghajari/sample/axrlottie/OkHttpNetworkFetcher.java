package com.aghajari.sample.axrlottie;

import android.content.Context;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.network.AXrLottieNetworkFetcher;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpNetworkFetcher extends AXrLottieNetworkFetcher {

    private OkHttpNetworkFetcher(){}

    public static OkHttpNetworkFetcher create(){
        return new OkHttpNetworkFetcher();
    }

    @Override
    protected void fetchFromNetwork(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.followRedirects(true).followSslRedirects(true);
                    builder.connectTimeout(AXrLottie.getNetworkTimeOut(), TimeUnit.MILLISECONDS);
                    builder.readTimeout(AXrLottie.getNetworkTimeOut(), TimeUnit.MILLISECONDS);

                    Request request = new Request.Builder().url(getURL()).build();
                    Response response = builder.build().newCall(request).execute();

                    if (response.isSuccessful()) {
                        String contentType = null;
                        if (response.body().contentType()!=null)
                            contentType = response.body().contentType().toString();

                        parseStream(context,response.body().byteStream(),contentType);
                    } else
                        onError(new Exception(response.message()+" : code="+response.code()));

                } catch (Exception e) {
                    onError(e);
                }
            }
        });
        thread.start();
    }
}
