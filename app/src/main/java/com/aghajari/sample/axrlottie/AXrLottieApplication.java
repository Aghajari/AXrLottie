package com.aghajari.sample.axrlottie;

import android.app.Application;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.extension.GZipFileExtension;

/**
 * @author kienht
 * @since 03/02/2021
 */
public class AXrLottieApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AXrLottie.init(this);
        //AXrLottie.setMaxNetworkCacheSize(100);
        AXrLottie.setNetworkFetcher(OkHttpNetworkFetcher.create());
        AXrLottie.addFileExtension(new GZipFileExtension(".tgs"));
    }
}
