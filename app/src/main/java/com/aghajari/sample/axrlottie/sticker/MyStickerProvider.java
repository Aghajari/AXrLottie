package com.aghajari.sample.axrlottie.sticker;

import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.emojiview.sticker.Sticker;
import com.aghajari.emojiview.sticker.StickerCategory;
import com.aghajari.emojiview.sticker.StickerLoader;
import com.aghajari.emojiview.sticker.StickerProvider;
import com.aghajari.rlottie.AXrLottieImageView;
import com.aghajari.sample.axrlottie.Utils;

public class MyStickerProvider implements StickerProvider {

    StickerCategory[] categories;

    public MyStickerProvider() {
        categories = new StickerCategory[]{
                new AnimatedStickerCategoty("HotCherry", 23),
                new AnimatedStickerCategoty("KangarooFighter", 26),
                new AnimatedStickerCategoty("ValentineCat", 15),
        };
    }

    @NonNull
    @Override
    public StickerCategory[] getCategories() {
        return categories;
    }

    @NonNull
    @Override
    public StickerLoader getLoader() {
        return new StickerLoader() {
            @Override
            public void onLoadSticker(View view, Sticker sticker) {
                if (view instanceof AXrLottieImageView && sticker instanceof AnimatedSticker) {
                    AXrLottieImageView lottieImageView = (AXrLottieImageView) view;
                    AnimatedSticker animatedSticker = (AnimatedSticker) sticker;
                    if (animatedSticker.drawable == null) {
                        animatedSticker.drawable = Utils.createFromSticker(view.getContext(), animatedSticker, 100);
                    }
                    lottieImageView.setLottieDrawable(animatedSticker.drawable);
                    lottieImageView.playAnimation();
                }
            }

            @Override
            public void onLoadStickerCategory(View view, StickerCategory stickerCategory, boolean selected) {
                if (view instanceof AXrLottieImageView) {
                    AXrLottieImageView lottieImageView = (AXrLottieImageView) view;
                    AnimatedSticker animatedSticker = (AnimatedSticker) stickerCategory.getCategoryData();
                    if (animatedSticker.drawable == null) {
                        animatedSticker.drawable = Utils.createFromSticker(view.getContext(), animatedSticker, 50);
                    }
                    lottieImageView.setLottieDrawable(animatedSticker.drawable);
                    //lottieImageView.playAnimation();
                }
            }
        };
    }

    @Override
    public boolean isRecentEnabled() {
        return true;
    }
}
