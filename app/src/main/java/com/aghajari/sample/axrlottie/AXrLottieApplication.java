package com.aghajari.sample.axrlottie;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.emojiview.AXEmojiManager;
import com.aghajari.emojiview.iosprovider.AXIOSEmojiProvider;
import com.aghajari.emojiview.listener.StickerViewCreatorListener;
import com.aghajari.emojiview.sticker.StickerCategory;
import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieDrawable;
import com.aghajari.rlottie.AXrLottieImageView;
import com.aghajari.rlottie.AXrLottieOptions;
import com.aghajari.rlottie.extension.GZipFileExtension;

public class AXrLottieApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AXrLottie.init(this);
      //AXrLottie.setMaxNetworkCacheSize(100);
        AXrLottie.setNetworkFetcher(OkHttpNetworkFetcher.create());
        AXrLottie.addFileExtension(new GZipFileExtension(".tgs"));

        AXrLottie.setDefaultOptions(new AXrLottieOptions()
                .setOnLottieLoaderListener(new AXrLottieDrawable.OnLottieLoaderListener() {
                    @Override
                    public void onLoaded(AXrLottieDrawable drawable) {
                        Log.d("AXrLottie", "AnimationLoaded : " + drawable.getCacheName());
                    }

                    @Override
                    public void onError(AXrLottieDrawable drawable, Throwable error) {
                        Log.e("AXrLottie", "Error : " + drawable.getCacheName(), error);
                    }
                }));
        initEmojiView();
    }

    public void initEmojiView() {
        AXEmojiManager.install(this, new AXIOSEmojiProvider(this));

        AXEmojiManager.setStickerViewCreatorListener(new StickerViewCreatorListener() {
            @Override
            public View onCreateStickerView(@NonNull Context context, @Nullable StickerCategory category, boolean isRecent) {
                return new AXrLottieImageView(context);
            }

            @Override
            public View onCreateCategoryView(@NonNull Context context) {
                return new AXrLottieImageView(context);
            }
        });

        AXEmojiManager.getEmojiViewTheme().setFooterEnabled(true);
        AXEmojiManager.getEmojiViewTheme().setFooterSelectedItemColor(0xffFF4081);
        AXEmojiManager.getEmojiViewTheme().setSelectionColor(Color.TRANSPARENT);
        AXEmojiManager.getEmojiViewTheme().setSelectedColor(0xffFF4081);
        AXEmojiManager.getEmojiViewTheme().setCategoryColor(Color.WHITE);
        AXEmojiManager.getEmojiViewTheme().setFooterBackgroundColor(Color.WHITE);
        AXEmojiManager.getEmojiViewTheme().setAlwaysShowDivider(true);

        AXEmojiManager.getStickerViewTheme().setSelectionColor(0xffFF4081);
        AXEmojiManager.getStickerViewTheme().setSelectedColor(0xffFF4081);
        AXEmojiManager.getStickerViewTheme().setCategoryColor(Color.WHITE);
        AXEmojiManager.getStickerViewTheme().setAlwaysShowDivider(true);
    }
}