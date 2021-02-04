package com.aghajari.sample.axrlottie;

import android.content.Context;

import com.aghajari.rlottie.AXrLottieDrawable;
import com.aghajari.sample.axrlottie.sticker.AnimatedSticker;

public class Utils {

    public static AXrLottieDrawable createFromSticker(Context context, AnimatedSticker sticker, int size) {
        return AXrLottieDrawable.fromAssets(context, sticker.getData())
                .setCacheName(sticker.name)
                .setSize(size, size)
                .setCacheEnabled(true)
                .setFpsLimit(false)
                .setAutoRepeat(true)
                .build();
    }

}
