package com.aghajari.rlottie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.aghajari.rlottie.network.AXrDefaultLottieNetworkFetcher;
import com.aghajari.rlottie.network.AXrLottieNetworkCacheProvider;
import com.aghajari.rlottie.network.AXrLottieNetworkFetcher;
import com.aghajari.rlottie.network.AXrNetworkCache;
import com.aghajari.rlottie.network.AXrNetworkFetcher;

import java.io.File;

/**
 * @author kienht
 * @since 29/01/2021
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AXrL {

    @SuppressLint("StaticFieldLeak")
    public static Context applicationContext;

    private static AXrLottieNetworkFetcher fetcher;
    private static AXrLottieNetworkCacheProvider cacheProvider;

    private static volatile AXrNetworkFetcher networkFetcher;
    private static volatile AXrNetworkCache networkCache;

    private static boolean cacheEnabled = true;

    @Nullable
    private static Float screenRefreshRate;

    public static void setContext(Context context) {
        AXrL.applicationContext = context.getApplicationContext();
    }

    public static void setFetcher(AXrLottieNetworkFetcher customFetcher) {
        AXrL.fetcher = customFetcher;
    }

    public static void setCacheProvider(AXrLottieNetworkCacheProvider customProvider) {
        AXrL.cacheProvider = customProvider;
    }

    public static void setCacheEnabled(boolean cacheEnabled) {
        AXrL.cacheEnabled = cacheEnabled;
    }

    public static boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public static void setCacheSize(@Nullable Integer cacheSize) {
        if (cacheSize != null) {
            AXrLottieNative.configureModelCacheSize(cacheSize);
        }
    }

    public static float getScreenRefreshRate() {
        return screenRefreshRate;
    }

    public static void setScreenRefreshRate(Float screenRefreshRate) {
        if (screenRefreshRate == null && applicationContext != null) {
            loadScreenRefreshRate(applicationContext);
        } else {
            AXrL.screenRefreshRate = screenRefreshRate;
        }
    }

    @NonNull
    public static AXrNetworkFetcher networkFetcher() {
        AXrNetworkFetcher local = networkFetcher;
        if (local == null) {
            synchronized (AXrNetworkFetcher.class) {
                local = networkFetcher;
                if (local == null) {
                    networkFetcher = local = new AXrNetworkFetcher(networkCache(), fetcher != null ? fetcher : new AXrDefaultLottieNetworkFetcher());
                }
            }
        }
        return local;
    }

    @NonNull
    public static AXrNetworkCache networkCache() {
        AXrNetworkCache local = networkCache;
        if (local == null) {
            synchronized (AXrNetworkCache.class) {
                local = networkCache;
                if (local == null) {
                    networkCache = local = new AXrNetworkCache(cacheProvider != null ? cacheProvider : new AXrLottieNetworkCacheProvider() {
                        @Override
                        @NonNull
                        public File getCacheDir() {
                            return new File(applicationContext.getCacheDir(), "lottie_network_cache");
                        }
                    });
                }
            }
        }
        return local;
    }

    public static void loadScreenRefreshRate(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            Display display = manager.getDefaultDisplay();
            if (display != null) {
                screenRefreshRate = display.getRefreshRate();
            }
        }
    }
}
