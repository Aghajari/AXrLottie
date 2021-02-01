package com.aghajari.rlottie.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * Class for custom library configuration.
 * This should be constructed with {@link AXrLottieConfig.Builder}
 */
public class AXrLottieConfig {

    @NonNull
    public final Context context;

    @Nullable
    public final AXrLottieNetworkFetcher networkFetcher;

    @Nullable
    public final AXrLottieNetworkCacheProvider cacheProvider;

    @NonNull
    public boolean cacheEnabled;

    @Nullable
    public Integer cacheSize;

    @Nullable
    public Float screenRefreshRate;

    private AXrLottieConfig(@NonNull Builder builder) {
        this.context = builder.context;
        this.networkFetcher = builder.networkFetcher;
        this.cacheProvider = builder.cacheProvider;
        this.cacheEnabled = builder.cacheEnabled;
        this.cacheSize = builder.cacheSize;
        this.screenRefreshRate = builder.screenRefreshRate;
    }

    public static final class Builder {

        @NonNull
        private Context context;

        @Nullable
        private AXrLottieNetworkFetcher networkFetcher;

        @Nullable
        private AXrLottieNetworkCacheProvider cacheProvider;

        @NonNull
        private boolean cacheEnabled = true;

        @Nullable
        public Integer cacheSize;

        @Nullable
        public Float screenRefreshRate;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        /**
         * Lottie has a default network fetching stack built on {@link java.net.HttpURLConnection}. However, if you would like to hook into your own
         * network stack for performance, caching, or analytics, you may replace the internal stack with your own.
         */
        @NonNull
        public Builder setNetworkFetcher(@NonNull AXrLottieNetworkFetcher fetcher) {
            this.networkFetcher = fetcher;
            return this;
        }

        /**
         * Provide your own network cache directory. By default, animations will be saved in your application's cacheDir/lottie_network_cache.
         *
         * @see #setNetworkCacheProvider(AXrLottieNetworkCacheProvider)
         */
        @NonNull
        public Builder setNetworkCacheDir(@NonNull final File file) {
            if (cacheProvider != null) {
                throw new IllegalStateException("There is already a cache provider!");
            }
            cacheProvider = new AXrLottieNetworkCacheProvider() {
                @Override
                @NonNull
                public File getCacheDir() {
                    if (!file.isDirectory()) {
                        throw new IllegalArgumentException("cache file must be a directory");
                    }
                    return file;
                }
            };
            return this;
        }

        /**
         * Provide your own network cache provider. By default, animations will be saved in your application's cacheDir/lottie_network_cache.
         */
        @NonNull
        public Builder setNetworkCacheProvider(@NonNull final AXrLottieNetworkCacheProvider fileCacheProvider) {
            if (cacheProvider != null) {
                throw new IllegalStateException("There is already a cache provider!");
            }
            cacheProvider = new AXrLottieNetworkCacheProvider() {
                @NonNull
                @Override
                public File getCacheDir() {
                    File file = fileCacheProvider.getCacheDir();
                    if (!file.isDirectory()) {
                        throw new IllegalArgumentException("cache file must be a directory");
                    }
                    return file;
                }
            };
            return this;
        }

        public Builder setCacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
            return this;
        }

        public Builder setScreenRefreshRate(@Nullable Float screenRefreshRate) {
            this.screenRefreshRate = screenRefreshRate;
            return this;
        }

        public Builder setCacheSize(@Nullable Integer cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        @NonNull
        public AXrLottieConfig build() {
            if (context == null) throw new IllegalArgumentException();
            return new AXrLottieConfig(this);
        }
    }
}