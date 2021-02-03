package com.aghajari.sample.axrlottie.sticker;

import com.aghajari.emojiview.sticker.Sticker;
import com.aghajari.rlottie.AXrLottieDrawable;

public class AnimatedSticker extends Sticker<String> {

    public String name;
    public transient AXrLottieDrawable drawable;

    public AnimatedSticker(String data, String name) {
        super(data);
        this.name = name;
        drawable = null;
    }

}
