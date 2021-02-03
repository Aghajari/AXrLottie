package com.aghajari.sample.axrlottie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.aghajari.sample.axrlottie.R;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieImageView;

public class SimpleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        final AXrLottieImageView lottieView = findViewById(R.id.lottie_view);

        lottieView.setLottieDrawable(
                AXrLottie.Loader.createFromAssets(this, "emoji_simple.json",
                        "emoji", 256, 256));

        /**lottieView.setLottieDrawable(
         AXrLottie.Loader.createFromAssets(this, "AnimatedSticker.tgs",
         "sticker", 256, 256));*/

        lottieView.playAnimation();

    }
}