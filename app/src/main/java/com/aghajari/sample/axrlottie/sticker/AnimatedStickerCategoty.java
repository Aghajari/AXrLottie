package com.aghajari.sample.axrlottie.sticker;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.emojiview.sticker.Sticker;
import com.aghajari.emojiview.sticker.StickerCategory;

public class AnimatedStickerCategoty implements StickerCategory<AnimatedSticker> {

    private AnimatedSticker[] stickers;

    public AnimatedStickerCategoty(String name, int stickerCount) {
        stickers = new AnimatedSticker[stickerCount];
        for (int i = 0; i < stickerCount; i++) {
            stickers[i] = new AnimatedSticker(name + "/sticker" + (i + 1) + ".json", name + ".sticker" + (i + 1));
        }
    }

    @NonNull
    @Override
    public Sticker[] getStickers() {
        return stickers;
    }

    @Override
    public AnimatedSticker getCategoryData() {
        return new AnimatedSticker(stickers[0].getData(), stickers[0].name);
    }

    @Override
    public boolean useCustomView() {
        return false;
    }

    @Override
    public View getView(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void bindView(View view) {
    }

    @Override
    public View getEmptyView(ViewGroup viewGroup) {
        return null;
    }
}
